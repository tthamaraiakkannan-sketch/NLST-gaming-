package com.example.media

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

enum class VoiceEffect(val id: String) {
    NORMAL("NORMAL"),
    BOY("BOY"),
    DEEP("DEEP"),
    ROBOT("ROBOT");

    companion object {
        fun fromId(id: String): VoiceEffect = entries.firstOrNull { it.id == id } ?: NORMAL
    }
}

data class VoiceEngineState(
    val isRunning: Boolean = false,
    val activeEffect: VoiceEffect = VoiceEffect.NORMAL,
    val inputDb: Float = -60f,
    val waveformBars: List<Float> = List(24) { 0.06f },
    val errorMessage: String? = null,
)

/**
 * Real-time on-device PCM voice DSP engine using AudioRecord + AudioTrack.
 * Applies Boy (pitch up), Deep (pitch down + low-pass), and Robot (ring modulation + comb filter)
 * directly to live microphone input frames.
 */
class VoiceEffectsEngine(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var captureJob: Job? = null

    private val _state = MutableStateFlow(VoiceEngineState())
    val state: StateFlow<VoiceEngineState> = _state.asStateFlow()

    @Volatile
    private var currentEffect: VoiceEffect = VoiceEffect.NORMAL

    fun setEffect(effect: VoiceEffect) {
        currentEffect = effect
        _state.update { it.copy(activeEffect = effect) }
    }

    fun hasRecordPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun start() {
        if (_state.value.isRunning) return
        if (!hasRecordPermission()) {
            _state.update { it.copy(errorMessage = "RECORD_AUDIO permission is required.") }
            return
        }

        _state.update { it.copy(isRunning = true, errorMessage = null) }

        captureJob = scope.launch {
            val sampleRate = 22050
            val channelIn = AudioFormat.CHANNEL_IN_MONO
            val channelOut = AudioFormat.CHANNEL_OUT_MONO
            val encoding = AudioFormat.ENCODING_PCM_16BIT

            val minRecBuf = AudioRecord.getMinBufferSize(sampleRate, channelIn, encoding)
                .coerceAtLeast(2048)
            val frameSize = 512

            val audioRecord = try {
                AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    sampleRate,
                    channelIn,
                    encoding,
                    minRecBuf * 2,
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isRunning = false,
                        errorMessage = "AudioRecord unavailable: ${e.message}",
                    )
                }
                return@launch
            }

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord.release()
                _state.update {
                    it.copy(
                        isRunning = false,
                        errorMessage = "Microphone failed to initialize on this device.",
                    )
                }
                return@launch
            }

            val minTrackBuf = AudioTrack.getMinBufferSize(sampleRate, channelOut, encoding)
                .coerceAtLeast(2048)

            val audioTrack = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(encoding)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelOut)
                            .build(),
                    )
                    .setBufferSizeInBytes(minTrackBuf * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                audioRecord.release()
                _state.update {
                    it.copy(
                        isRunning = false,
                        errorMessage = "AudioTrack unavailable: ${e.message}",
                    )
                }
                return@launch
            }

            val inputBuffer = ShortArray(frameSize)
            val outputBuffer = ShortArray(frameSize)

            // Granular ring buffer for overlap-add pitch shifting
            val ringSize = 2048
            val ringBuffer = FloatArray(ringSize)
            var writePos = 0
            var readPhaseA = 0f
            var readPhaseB = ringSize / 2f

            // Robot ring modulator & comb delay state
            var carrierPhase = 0.0
            val combSize = 96
            val combDelay = FloatArray(combSize)
            var combIdx = 0

            // Deep voice low-pass filter state
            var lowPassPrev = 0f

            try {
                audioRecord.startRecording()
                audioTrack.play()

                while (isActive && _state.value.isRunning) {
                    val readCount = audioRecord.read(inputBuffer, 0, frameSize)
                    if (readCount <= 0) continue

                    // Measure RMS dB & waveform bars from live PCM input
                    var sumSq = 0.0
                    for (i in 0 until readCount) {
                        val s = inputBuffer[i] / 32768.0
                        sumSq += s * s
                    }
                    val rms = sqrt(sumSq / readCount).coerceAtLeast(1e-6)
                    val db = (20.0 * log10(rms)).toFloat().coerceIn(-60f, 0f)

                    val bars = List(24) { barIndex ->
                        val sliceStart = (barIndex * readCount) / 24
                        val sliceEnd = (((barIndex + 1) * readCount) / 24).coerceAtLeast(sliceStart + 1)
                        var peak = 0f
                        for (j in sliceStart until sliceEnd.coerceAtMost(readCount)) {
                            val norm = abs(inputBuffer[j] / 32768f)
                            if (norm > peak) peak = norm
                        }
                        (peak * 3.2f).coerceIn(0.06f, 1.0f)
                    }

                    val effect = currentEffect
                    when (effect) {
                        VoiceEffect.NORMAL -> {
                            System.arraycopy(inputBuffer, 0, outputBuffer, 0, readCount)
                        }

                        VoiceEffect.BOY -> {
                            // Pitch up ~1.38x using two Hann-windowed granular read heads
                            val pitchFactor = 1.38f
                            for (i in 0 until readCount) {
                                ringBuffer[writePos] = inputBuffer[i].toFloat()
                                val aIdx = ((readPhaseA.toInt() % ringSize) + ringSize) % ringSize
                                val bIdx = ((readPhaseB.toInt() % ringSize) + ringSize) % ringSize
                                val phaseDiff = ((readPhaseA - writePos + ringSize) % ringSize) / ringSize.toFloat()
                                val winA = 0.5f * (1f - cos(2f * PI.toFloat() * phaseDiff))
                                val winB = 1f - winA
                                val sample = ringBuffer[aIdx] * winA + ringBuffer[bIdx] * winB
                                outputBuffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                                writePos = (writePos + 1) % ringSize
                                readPhaseA = (readPhaseA + pitchFactor) % ringSize
                                readPhaseB = (readPhaseB + pitchFactor) % ringSize
                            }
                        }

                        VoiceEffect.DEEP -> {
                            // Pitch down ~0.72x + warm low-pass bass emphasis
                            val pitchFactor = 0.72f
                            for (i in 0 until readCount) {
                                ringBuffer[writePos] = inputBuffer[i].toFloat()
                                val aIdx = ((readPhaseA.toInt() % ringSize) + ringSize) % ringSize
                                val bIdx = ((readPhaseB.toInt() % ringSize) + ringSize) % ringSize
                                val phaseDiff = ((readPhaseA - writePos + ringSize) % ringSize) / ringSize.toFloat()
                                val winA = 0.5f * (1f - cos(2f * PI.toFloat() * phaseDiff))
                                val winB = 1f - winA
                                val rawPitched = ringBuffer[aIdx] * winA + ringBuffer[bIdx] * winB
                                lowPassPrev = 0.58f * rawPitched + 0.42f * lowPassPrev
                                val boosted = lowPassPrev * 1.15f
                                outputBuffer[i] = boosted.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                                writePos = (writePos + 1) % ringSize
                                readPhaseA = (readPhaseA + pitchFactor) % ringSize
                                readPhaseB = (readPhaseB + pitchFactor) % ringSize
                            }
                        }

                        VoiceEffect.ROBOT -> {
                            // Ring modulation at 115 Hz + metallic comb delay
                            val step = 2.0 * PI * 115.0 / sampleRate
                            for (i in 0 until readCount) {
                                val x = inputBuffer[i].toFloat()
                                val mod = sin(carrierPhase).toFloat()
                                carrierPhase = (carrierPhase + step) % (2.0 * PI)
                                val modulated = x * (0.45f + 0.65f * mod)
                                val delayed = combDelay[combIdx]
                                val combined = modulated * 0.72f + delayed * 0.45f
                                combDelay[combIdx] = combined.coerceIn(-32767f, 32767f)
                                combIdx = (combIdx + 1) % combSize
                                outputBuffer[i] = combined.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                            }
                        }
                    }

                    audioTrack.write(outputBuffer, 0, readCount)
                    _state.update {
                        it.copy(
                            inputDb = db,
                            waveformBars = bars,
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = e.message ?: "Voice DSP stream interrupted")
                }
            } finally {
                runCatching { audioRecord.stop() }
                runCatching { audioRecord.release() }
                runCatching { audioTrack.stop() }
                runCatching { audioTrack.release() }
                _state.update {
                    it.copy(
                        isRunning = false,
                        inputDb = -60f,
                        waveformBars = List(24) { 0.06f },
                    )
                }
            }
        }
    }

    fun stop() {
        _state.update { it.copy(isRunning = false) }
        captureJob?.cancel()
        captureJob = null
    }
}

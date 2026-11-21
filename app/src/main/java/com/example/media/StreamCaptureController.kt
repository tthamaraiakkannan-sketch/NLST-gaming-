package com.example.media

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class StreamQuality(val label: String, val width: Int, val height: Int, val targetFps: Int) {
    HD_720("720p • 60 FPS", 1280, 720, 60),
    FHD_1080("1080p • 60 FPS", 1920, 1080, 60),
    SD_480("480p • Low Latency", 854, 480, 30),
}

enum class StreamOrientation(val label: String) {
    LANDSCAPE("Landscape (16:9)"),
    PORTRAIT("Portrait (9:16)"),
}

enum class GameCaptureTarget(val title: String, val packageName: String?) {
    FREE_FIRE("Garena Free Fire", "com.dts.freefireth"),
    FREE_FIRE_MAX("Free Fire MAX", "com.dts.freefiremax"),
    ANY_GAME("Full Device Screen", null),
}

data class ScreenCaptureState(
    val isCapturing: Boolean = false,
    val targetGame: GameCaptureTarget = GameCaptureTarget.FREE_FIRE,
    val quality: StreamQuality = StreamQuality.HD_720,
    val orientation: StreamOrientation = StreamOrientation.LANDSCAPE,
    val measuredFps: Int = 0,
    val capturedWidth: Int = 0,
    val capturedHeight: Int = 0,
    val elapsedSeconds: Long = 0L,
    val totalFramesCaptured: Long = 0L,
    val lastError: String? = null,
)

/**
 * Shared state holder bridging Compose UI and StreamForegroundService MediaProjection lifecycle.
 */
object StreamCaptureRegistry {
    private val _state = MutableStateFlow(ScreenCaptureState())
    val state: StateFlow<ScreenCaptureState> = _state.asStateFlow()

    fun updateConfig(
        targetGame: GameCaptureTarget = _state.value.targetGame,
        quality: StreamQuality = _state.value.quality,
        orientation: StreamOrientation = _state.value.orientation,
    ) {
        _state.update {
            it.copy(
                targetGame = targetGame,
                quality = quality,
                orientation = orientation,
            )
        }
    }

    internal fun onCaptureStarted(width: Int, height: Int) {
        _state.update {
            it.copy(
                isCapturing = true,
                capturedWidth = width,
                capturedHeight = height,
                measuredFps = 0,
                elapsedSeconds = 0L,
                totalFramesCaptured = 0L,
                lastError = null,
            )
        }
    }

    internal fun onTelemetryTick(fps: Int, elapsedSeconds: Long, totalFrames: Long) {
        _state.update {
            it.copy(
                measuredFps = fps,
                elapsedSeconds = elapsedSeconds,
                totalFramesCaptured = totalFrames,
            )
        }
    }

    internal fun onCaptureStopped(error: String? = null) {
        _state.update {
            it.copy(
                isCapturing = false,
                measuredFps = 0,
                lastError = error,
            )
        }
    }

    fun stopCapture(context: Context) {
        val intent = Intent(context, StreamForegroundService::class.java).apply {
            action = StreamForegroundService.ACTION_STOP
        }
        context.startService(intent)
    }
}

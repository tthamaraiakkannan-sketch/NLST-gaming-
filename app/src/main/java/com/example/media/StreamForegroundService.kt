package com.example.media

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.concurrent.atomic.AtomicLong

class StreamForegroundService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var handlerThread: HandlerThread? = null
    private var captureHandler: Handler? = null
    private var telemetryRunnable: Runnable? = null

    private val frameCounter = AtomicLong(0L)
    private val totalFrames = AtomicLong(0L)
    private var startTimeMs = 0L

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            stopCaptureInternal(null)
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopCaptureInternal(null)
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val resultData: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_DATA)
                }
                if (resultCode != Activity.RESULT_OK || resultData == null) {
                    StreamCaptureRegistry.onCaptureStopped("Screen capture permission was not granted.")
                    stopSelf()
                    return START_NOT_STICKY
                }
                promoteToForeground()
                startMediaProjection(resultCode, resultData)
            }
        }
        return START_NOT_STICKY
    }

    private fun promoteToForeground() {
        createNotificationChannel()
        val notification = buildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasMicPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED

            val serviceTypes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasMicPermission) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            }
            startForeground(NOTIFICATION_ID, notification, serviceTypes)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startMediaProjection(resultCode: Int, data: Intent) {
        try {
            val projectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val projection = projectionManager.getMediaProjection(resultCode, data)
            if (projection == null) {
                StreamCaptureRegistry.onCaptureStopped("MediaProjection returned null.")
                stopSelf()
                return
            }

            val config = StreamCaptureRegistry.state.value
            val captureW = if (config.orientation == StreamOrientation.LANDSCAPE) {
                config.quality.width
            } else {
                config.quality.height
            }
            val captureH = if (config.orientation == StreamOrientation.LANDSCAPE) {
                config.quality.height
            } else {
                config.quality.width
            }
            val densityDpi = resources.displayMetrics.densityDpi

            val thread = HandlerThread("NlstScreenCapture").apply { start() }
            val handler = Handler(thread.looper)
            handlerThread = thread
            captureHandler = handler

            projection.registerCallback(projectionCallback, handler)
            mediaProjection = projection

            val reader = ImageReader.newInstance(captureW, captureH, PixelFormat.RGBA_8888, 2)
            reader.setOnImageAvailableListener({ r ->
                val image = runCatching { r.acquireLatestImage() }.getOrNull()
                if (image != null) {
                    frameCounter.incrementAndGet()
                    totalFrames.incrementAndGet()
                    image.close()
                }
            }, handler)
            imageReader = reader

            virtualDisplay = projection.createVirtualDisplay(
                "NLST_Gaming_Capture",
                captureW,
                captureH,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.surface,
                null,
                handler,
            )

            startTimeMs = System.currentTimeMillis()
            frameCounter.set(0L)
            totalFrames.set(0L)
            StreamCaptureRegistry.onCaptureStarted(captureW, captureH)

            val tick = object : Runnable {
                override fun run() {
                    if (mediaProjection == null) return
                    val fps = frameCounter.getAndSet(0L).toInt()
                    val elapsedSec = ((System.currentTimeMillis() - startTimeMs) / 1000L).coerceAtLeast(0L)
                    StreamCaptureRegistry.onTelemetryTick(
                        fps = fps,
                        elapsedSeconds = elapsedSec,
                        totalFrames = totalFrames.get(),
                    )
                    captureHandler?.postDelayed(this, 1000L)
                }
            }
            telemetryRunnable = tick
            handler.postDelayed(tick, 1000L)
        } catch (e: Exception) {
            stopCaptureInternal(e.message ?: "Failed to start screen capture")
            stopSelf()
        }
    }

    private fun stopCaptureInternal(error: String?) {
        telemetryRunnable?.let { captureHandler?.removeCallbacks(it) }
        telemetryRunnable = null
        runCatching { virtualDisplay?.release() }
        virtualDisplay = null
        runCatching { imageReader?.close() }
        imageReader = null
        runCatching {
            mediaProjection?.unregisterCallback(projectionCallback)
            mediaProjection?.stop()
        }
        mediaProjection = null
        runCatching { handlerThread?.quitSafely() }
        handlerThread = null
        captureHandler = null
        StreamCaptureRegistry.onCaptureStopped(error)
    }

    override fun onDestroy() {
        stopCaptureInternal(null)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.stream_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE,
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, StreamForegroundService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.stream_notification_title))
            .setContentText(getString(R.string.stream_notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppIntent)
            .addAction(0, getString(R.string.action_stop_capture), stopIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "com.example.media.ACTION_START_CAPTURE"
        const val ACTION_STOP = "com.example.media.ACTION_STOP_CAPTURE"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
        private const val CHANNEL_ID = "nlst_stream_capture_channel"
        private const val NOTIFICATION_ID = 4109

        fun startIntent(context: Context, resultCode: Int, data: Intent): Intent =
            Intent(context, StreamForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_RESULT_DATA, data)
            }
    }
}

package com.benjaminearley.zapdos

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.WindowManager
import java.io.File

import java.io.IOException

class ScreenRecorderService : Service() {

    private var mediaProjectionManager: MediaProjectionManager? = null

    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private var mediaProjection: MediaProjection? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        if (ACTION_START == action) {
            val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
            startRecordingService(resultCode, intent)
        } else if (ACTION_STOP == action) {
            stopReocrdingService()
        }
        return Service.START_STICKY
    }

    private fun startRecordingService(resultCode: Int, intent: Intent) {
        Log.v(TAG, "Starting Recording")
        mediaProjection = mediaProjectionManager!!.getMediaProjection(resultCode, intent)
        mediaProjectionCallback = MediaProjectionCallback()
        mediaProjection!!.registerCallback(mediaProjectionCallback, null)
        mediaRecorder = MediaRecorder()
        initRecorder()
        shareScreen()
        virtualDisplay = createVirtualDisplay()
    }

    private fun stopReocrdingService() {
        mediaRecorder!!.stop()
        mediaRecorder!!.reset()
        Log.v(TAG, "Stopping Recording")
        stopScreenSharing()
    }

    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var screenDensity: Int = 0

    private fun shareScreen() {
        mediaRecorder!!.start()
    }

    private fun createVirtualDisplay(): VirtualDisplay {

        val metrics = DisplayMetrics()
        val window = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        window.defaultDisplay.getMetrics(metrics)
        screenDensity = metrics.densityDpi

        return mediaProjection!!.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder!!.surface, null, null)
    }

    private fun initRecorder() {
        try {
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

            val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "pokesnap")
            if (!f.exists()) {
                f.mkdirs()
            }

            mediaRecorder!!.setOutputFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/pokesnap/catch+" + System.currentTimeMillis() / 1000L + ".mp4")
            mediaRecorder!!.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder!!.setVideoEncodingBitRate(2097152)
            mediaRecorder!!.setVideoFrameRate(30)
            val window = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val rotation = window.defaultDisplay.rotation
            val orientation = ORIENTATIONS.get(rotation + 90)
            mediaRecorder!!.setOrientationHint(orientation)
            mediaRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            mediaRecorder!!.stop()
            mediaRecorder!!.reset()
            Log.v(TAG, "Recording Stopped")

            mediaProjection = null
            stopScreenSharing()
        }
    }

    private fun stopScreenSharing() {
        if (virtualDisplay == null) {
            return
        }
        virtualDisplay!!.release()
        mediaRecorder!!.release()
        destroyMediaProjection()
    }

    private var mediaProjectionCallback: MediaProjectionCallback? = null

    private fun destroyMediaProjection() {
        if (mediaProjection != null) {
            mediaProjection!!.unregisterCallback(mediaProjectionCallback)
            mediaProjection!!.stop()
            mediaProjection = null
        }
        Log.i(TAG, "MediaProjection Stopped")
        stopSelf()
    }

    companion object {

        private val TAG = "ScreenRecorderService"

        private val BASE = "com.benjaminearley.zapdos.service.ScreenRecorderService."
        val ACTION_START = BASE + "ACTION_START"
        val ACTION_STOP = BASE + "ACTION_STOP"
        val EXTRA_RESULT_CODE = BASE + "EXTRA_RESULT_CODE"

        private val DISPLAY_WIDTH = 720
        private val DISPLAY_HEIGHT = 1280

        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

}
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

import java.io.IOException

class ScreenRecorderService : Service() {

    private var mMediaProjectionManager: MediaProjectionManager? = null

    override fun onCreate() {
        super.onCreate()
        mMediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private var mMediaProjection: MediaProjection? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        if (ACTION_START == action) {
            val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
            try {
                startRecordingService(resultCode, intent)
            } catch (e: IllegalStateException) {
                stopReocrdingService()
                startRecordingService(resultCode, intent)
            }

        } else if (ACTION_STOP == action) {
            stopReocrdingService()
        }
        return Service.START_STICKY
    }

    private fun startRecordingService(resultCode: Int, intent: Intent) {
        Log.v(TAG, "Starting Recording")
        mMediaProjection = mMediaProjectionManager!!.getMediaProjection(resultCode, intent)
        mMediaProjectionCallback = MediaProjectionCallback()
        mMediaProjection!!.registerCallback(mMediaProjectionCallback, null)
        mMediaRecorder = MediaRecorder()
        initRecorder()
        shareScreen()
        mVirtualDisplay = createVirtualDisplay()
    }

    private fun stopReocrdingService() {
        mMediaRecorder!!.stop()
        mMediaRecorder!!.reset()
        Log.v(TAG, "Stopping Recording")
        stopScreenSharing()
    }

    private var mMediaRecorder: MediaRecorder? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mScreenDensity: Int = 0

    private fun shareScreen() {
        mMediaRecorder!!.start()
    }

    private fun createVirtualDisplay(): VirtualDisplay {

        val metrics = DisplayMetrics()
        val window = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        window.defaultDisplay.getMetrics(metrics)
        mScreenDensity = metrics.densityDpi

        return mMediaProjection!!.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder!!.surface, null, null)
    }

    private fun initRecorder() {
        try {
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mMediaRecorder!!.setOutputFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/catch+" + System.currentTimeMillis() / 1000L + ".mp4")
            mMediaRecorder!!.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mMediaRecorder!!.setVideoEncodingBitRate(2097152)
            mMediaRecorder!!.setVideoFrameRate(30)
            val window = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val rotation = window.defaultDisplay.rotation
            val orientation = ORIENTATIONS.get(rotation + 90)
            mMediaRecorder!!.setOrientationHint(orientation)
            mMediaRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            mMediaRecorder!!.stop()
            mMediaRecorder!!.reset()
            Log.v(TAG, "Recording Stopped")

            mMediaProjection = null
            stopScreenSharing()
        }
    }

    private fun stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        mMediaRecorder!!.release()
        destroyMediaProjection()
    }

    private var mMediaProjectionCallback: MediaProjectionCallback? = null

    private fun destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection!!.unregisterCallback(mMediaProjectionCallback)
            mMediaProjection!!.stop()
            mMediaProjection = null
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
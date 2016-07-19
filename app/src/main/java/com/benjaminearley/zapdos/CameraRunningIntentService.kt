package com.benjaminearley.zapdos

import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.NotificationCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager
import java.util.*
import java.util.concurrent.TimeUnit

var START_FOREGROUND_ACTION = "com.benjaminearley.zapdos.action.startforeground"
var STOP_FOREGROUND_ACTION = "com.benjaminearley.zapdos.action.stopforeground"

class CameraRunningIntentService : Service(), FloatingViewListener {

    private val mBinder = MyBinder()
    var resultCode: Int? = null
    var intentData: Intent? = null

    var inflater: LayoutInflater? = null
    var iconView: ImageView? = null
    var metrics: DisplayMetrics? = null
    var mFloatingViewManager: FloatingViewManager? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action.equals(START_FOREGROUND_ACTION)) {

            metrics = DisplayMetrics()
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(metrics)

            val notification = NotificationCompat.Builder(this).setContentText("Test").build()

            startForeground(101, notification)

            var cameraInUse = false
            var currentlyRecording = false

            val handler = Handler()
            val runnable = Runnable {
                currentlyRecording = false
                val startIntent = Intent(this@CameraRunningIntentService, ScreenRecorderService::class.java)
                startIntent.action = ScreenRecorderService.ACTION_STOP
                startService(startIntent)

                createHeadView()
            }

            val camAvailCallback = object : CameraManager.AvailabilityCallback() {
                override fun onCameraAvailable(cameraId: String) {
                    if (cameraInUse) {

                        cameraInUse = false
                        Log.d("Camera: ", "notified that camera is not in use.")

                        handler.postDelayed(runnable, TimeUnit.SECONDS.toMillis(9))

                    }

                }

                override fun onCameraUnavailable(cameraId: String) {

                    if (checkForegroundTaskIsPokemon()) {

                        cameraInUse = true
                        Log.d("Camera: ", "notified that camera is in use.")


                        handler.removeCallbacks(runnable)

                        if (intentData?.extras != null && !currentlyRecording) {
                            currentlyRecording = true
                            val startIntent = Intent(this@CameraRunningIntentService, ScreenRecorderService::class.java)
                            startIntent.action = ScreenRecorderService.ACTION_START
                            startIntent.putExtra(ScreenRecorderService.EXTRA_RESULT_CODE, resultCode)
                            startIntent.putExtras(intentData?.extras)
                            startService(startIntent)
                        }
                    }
                }
            }

            val cam_manager = getSystemService(CAMERA_SERVICE) as CameraManager
            cam_manager.registerAvailabilityCallback(camAvailCallback, null)

        } else if (intent?.action.equals(STOP_FOREGROUND_ACTION)) {
            stopForeground(true)
            stopSelf()
        }

        return Service.START_STICKY
    }

    private fun createHeadView() {

        inflater = LayoutInflater.from(this)
        iconView = inflater?.inflate(R.layout.widget_chathead, null, false) as ImageView
        iconView?.setOnClickListener {

            try {
                mFloatingViewManager?.removeAllViewToWindow()
            } catch (ignored: Exception) {}

            val captureIntent = Intent(this, CaptureActivity::class.java)
            captureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(captureIntent)
        }

        mFloatingViewManager = FloatingViewManager(this, this)
        mFloatingViewManager?.setFixedTrashIconImage(R.drawable.ic_trash_fixed)
        mFloatingViewManager?.setActionTrashIconImage(R.drawable.ic_trash_action)
        val options = FloatingViewManager.Options()
        options.shape = FloatingViewManager.SHAPE_CIRCLE
        options.overMargin = (16 * (metrics?.density?.toInt() ?: 0))
        try {
            mFloatingViewManager?.removeAllViewToWindow()
        } catch (ignored: Exception) {}
        mFloatingViewManager?.addViewToWindow(iconView, options)
    }

    private fun checkForegroundTaskIsPokemon(): Boolean {
        var currentApp = "NULL"

        val usm = this.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time)
        if (appList != null && appList.size > 0) {
            val mySortedMap = TreeMap<Long, UsageStats>()
            for (usageStats in appList) {
                mySortedMap.put(usageStats.lastTimeUsed, usageStats)
            }
            if (!mySortedMap.isEmpty()) {
                currentApp = mySortedMap[mySortedMap.lastKey()]?.packageName ?: "Unknown"
            }
        }
          //return currentApp.contains("camera", true)
        return currentApp.contains("com.nianticlabs.pokemongo", true)
    }

    override fun onFinishFloatingView() {
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    inner class MyBinder : Binder() {
        internal val service: CameraRunningIntentService
            get() = this@CameraRunningIntentService
    }
}

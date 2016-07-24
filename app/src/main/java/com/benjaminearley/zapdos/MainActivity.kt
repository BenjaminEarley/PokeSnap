package com.benjaminearley.zapdos

import android.annotation.TargetApi
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    var mediaProjectionManager: MediaProjectionManager? = null
    var boundCameraRunningService: CameraRunningIntentService? = null
    var serviceBound = false
    var startIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

//        MobileAds.initialize(this, getString(R.string.banner_ad_unit_id))
//
//        if (!BuildConfig.DEBUG) {
//            val adRequest = AdRequest.Builder()
//                    .build()
//            adView?.loadAd(adRequest)
//        }

        val listAdapter = VideoTileListAdapter(
                getVideoThumbnails(
                        File(
                        Environment
                                .getExternalStoragePublicDirectory(
                                        Environment
                                                .DIRECTORY_MOVIES)
                                .toString() + "/pokesnap")))

        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = listAdapter

        startIntent = Intent(this@MainActivity, CameraRunningIntentService::class.java)
        startIntent?.action = START_FOREGROUND_ACTION
        startService(startIntent)
        bindService(startIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        getScreenShotPermission()
    }

    fun getVideoThumbnails(dir: File): ArrayList<Bitmap> {
        val thumbnails: ArrayList<Bitmap> = ArrayList()
        if (dir.exists()) {
            val files = dir.listFiles()
            for (file in files) {
                if (!file.isDirectory) {
                    thumbnails.add(
                            ThumbnailUtils.createVideoThumbnail(
                                    file.absolutePath,
                                    MediaStore.Video.Thumbnails.MINI_KIND))
                }
            }
        }
        return thumbnails
    }

    override fun onResume() {
        super.onResume()
       // if (!BuildConfig.DEBUG) adView?.resume()
    }

    override fun onPause() {
       // if (!BuildConfig.DEBUG) adView?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
        super.onDestroy()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun getScreenShotPermission() {

        mediaProjectionManager = this.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager?.createScreenCaptureIntent(), 1)

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                boundCameraRunningService?.resultCode = resultCode
                boundCameraRunningService?.intentData = data
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val myBinder = service as CameraRunningIntentService.MyBinder
            boundCameraRunningService = myBinder.service
            serviceBound = true
        }
    }
}

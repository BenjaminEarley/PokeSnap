package com.benjaminearley.zapdos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.afollestad.easyvideoplayer.EasyVideoCallback
import com.afollestad.easyvideoplayer.EasyVideoPlayer

class VideoPlaybackActivity : AppCompatActivity(), EasyVideoCallback {

    var player: EasyVideoPlayer? = null
    var appBarLayout: AppBarLayout? = null

    companion object {

        val ARG_VIDEO_FILE = "videoFile"

        fun show(activity: Activity, filePath: String) {

            val intent = Intent(activity, VideoPlaybackActivity::class.java)
            intent.putExtra(ARG_VIDEO_FILE, filePath)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_playback)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.navigationIcon = ContextCompat.getDrawable(
                this,
                R.drawable.ic_arrow_back_white_24dp)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener({
            super.onBackPressed()
        })

        appBarLayout = findViewById(R.id.appbar) as AppBarLayout

        player = findViewById(R.id.player) as EasyVideoPlayer
        player?.setSource(Uri.parse("file://" + intent?.extras?.getString(ARG_VIDEO_FILE, "")))
        player?.setCallback(this)
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }
    
    override fun onPrepared(player: EasyVideoPlayer?) {}

    override fun onStarted(player: EasyVideoPlayer?) {}

    override fun onCompletion(player: EasyVideoPlayer?) {}

    override fun onRetry(player: EasyVideoPlayer?, source: Uri?) {}

    override fun onSubmit(player: EasyVideoPlayer?, source: Uri?) {}

    override fun onBuffering(percent: Int) {}

    override fun onPreparing(player: EasyVideoPlayer?) {}

    override fun onError(player: EasyVideoPlayer?, e: Exception?) {

        AlertDialog
                .Builder(this)
                .setTitle(R.string.error)
                .setMessage(e?.message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    override fun onPaused(player: EasyVideoPlayer?) {}

    override fun controlsAreShown() {
        appBarLayout?.animate()?.cancel()
        appBarLayout?.alpha = 0f
        appBarLayout?.visibility = View.VISIBLE
        appBarLayout?.animate()?.alpha(1f)?.setInterpolator(DecelerateInterpolator())?.start()
    }

    override fun controlsAreHidden() {
        appBarLayout?.animate()?.cancel()
        appBarLayout?.alpha = 1f
        appBarLayout?.visibility = View.VISIBLE
        appBarLayout?.animate()?.alpha(0f)?.setInterpolator(DecelerateInterpolator())?.start()
    }
}

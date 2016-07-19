package com.benjaminearley.zapdos.onboarding

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import com.benjaminearley.zapdos.R
import kotlinx.android.synthetic.main.activity_splash_animation.*

class SplashAnimationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_animation)

        val handler = Handler()
        handler.postDelayed({
                    val intent = Intent(this, OnboardingActivity::class.java)
                    startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this, pokeBall as View, pokeBall.transitionName).toBundle())
                    overridePendingTransition(R.anim.fade_in, 0)
                }, 100)

    }
}

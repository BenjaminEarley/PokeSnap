package com.benjaminearley.zapdos.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.benjaminearley.zapdos.MainActivity
import com.benjaminearley.zapdos.R
import com.benjaminearley.zapdos.onboarding.OnboardingActivity
import com.benjaminearley.zapdos.onboarding.SplashAnimationActivity

fun shouldShowOnboarding(c: Context): Boolean {
    return PreferenceManager
            .getDefaultSharedPreferences(c)
            .getBoolean(c.getString(R.string.show_onboarding_key), true)
}

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (shouldShowOnboarding(this)) {
            val intent = Intent(this, SplashAnimationActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

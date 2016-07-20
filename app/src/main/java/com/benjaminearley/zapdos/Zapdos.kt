package com.benjaminearley.zapdos

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import io.fabric.sdk.android.Fabric

class Zapdos : Application() {

    override fun onCreate() {
        super.onCreate()
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        Fabric.with(this, Crashlytics())
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

}
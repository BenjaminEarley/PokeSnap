package com.benjaminearley.zapdos

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View

class CaptureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = ""
        toolbar.navigationIcon = ContextCompat.getDrawable(
                this,
                R.drawable.ic_arrow_back_white_24dp)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener({
            onBackPressed()
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }
}

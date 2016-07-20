package com.benjaminearley.zapdos.onboarding

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.Scroller

class NonSwipeableViewPager : ViewPager {

    private var scroller: FixedSpeedScroller? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        try {
            val viewpager = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("scroller")
            scroller.isAccessible = true
            this.scroller = FixedSpeedScroller(context,
                    DecelerateInterpolator())
            scroller.set(this, this.scroller)
        } catch (ignored: Exception) {
        }

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    private inner class FixedSpeedScroller : Scroller {

        private var scrollDuration = 350

        constructor(context: Context) : super(context) {
        }

        constructor(context: Context, interpolator: Interpolator) : super(context, interpolator) {
        }

        constructor(context: Context, interpolator: Interpolator, flywheel: Boolean) : super(context, interpolator, flywheel) {
        }

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, scrollDuration)
        }

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, scrollDuration)
        }

        fun setScrollDuration(duration: Int) {
            scrollDuration = duration
        }
    }
}

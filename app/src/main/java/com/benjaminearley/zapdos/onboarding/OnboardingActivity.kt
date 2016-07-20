package com.benjaminearley.zapdos.onboarding

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.benjaminearley.zapdos.MainActivity
import com.benjaminearley.zapdos.R
import com.benjaminearley.zapdos.util.hasExternalStoragePermission
import com.benjaminearley.zapdos.util.hasMicPermission

fun disableOnboarding(context: Context) {
    val applicationContext = context.applicationContext
    return PreferenceManager
            .getDefaultSharedPreferences(applicationContext)
            .edit()
            .putBoolean(applicationContext.getString(R.string.show_onboarding_key), false)
            .apply()
}

fun canMakeSmores(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

val PERMISSION_SETTINGS_REQUEST_CODE = 2
val APPS_WITH_USAGE_SETTINGS_REQUEST_CODE = 3

fun startInstalledAppDetailsActivity(activity: Activity) {

    val i = Intent()
    i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    i.addCategory(Intent.CATEGORY_DEFAULT)
    i.data = Uri.parse("package:" + activity.packageName)
    activity.startActivityForResult(i, PERMISSION_SETTINGS_REQUEST_CODE)
}

fun startOverlayPermissionActivity(activity: Activity) {
    if (!Settings.canDrawOverlays(activity)) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.packageName))
        activity.startActivityForResult(intent, PERMISSION_SETTINGS_REQUEST_CODE)
    }
}

fun startMainActivity(activity: Activity) {
    disableOnboarding(activity)
    val intent = Intent(activity, MainActivity::class.java)
    activity.startActivity(intent)
    activity.finish()
}

class OnboardingActivity : AppCompatActivity() {

    val MANIFEST_REQUEST_CODE = 0
    val SCREEN_RECORD_REQUEST_CODE = 1

    var sectionsPagerAdapter: SectionsPagerAdapter? = null

    var viewPager: ViewPager? = null

    var mediaProjectionManager: MediaProjectionManager? = null

    var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        currentPage = if (canMakeSmores() && (!hasExternalStoragePermission(this) || !hasMicPermission(this))) 0 else 1

        sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        viewPager = findViewById(R.id.container) as ViewPager
        viewPager?.adapter = sectionsPagerAdapter
        viewPager?.currentItem = currentPage

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener({ view -> nextStep() })
    }

    fun nextStep() = when (currentPage) {
        0 -> handlePermissions()
        1 -> getScreenShotPermission()
        2 -> openOverlayDrawSetting()
        3 -> openAppsWithUsageAccess()
        else -> null
    }

    private fun openAppsWithUsageAccess() {
        startActivityForResult(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS), APPS_WITH_USAGE_SETTINGS_REQUEST_CODE)
    }

    fun nextPage(number: Int = 1) {
        currentPage += number
        viewPager?.setCurrentItem(currentPage, true)
    }


    @TargetApi(Build.VERSION_CODES.M)
    private fun handlePermissions() {

        val hasExternalWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val hasRecordAudioPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
        val permissions = arrayListOf<String>()
        if (hasExternalWritePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (hasRecordAudioPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this@OnboardingActivity, permissions.toArray(arrayOfNulls<String>(permissions.size)), MANIFEST_REQUEST_CODE)
        } else {
            nextPage()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) =
            if (requestCode == MANIFEST_REQUEST_CODE) nextPage()
            else {
            }


    fun getScreenShotPermission() {
        mediaProjectionManager = this.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager?.createScreenCaptureIntent(), SCREEN_RECORD_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SCREEN_RECORD_REQUEST_CODE -> if (canMakeSmores()) nextPage() else nextPage(2)
            PERMISSION_SETTINGS_REQUEST_CODE -> nextPage()
            APPS_WITH_USAGE_SETTINGS_REQUEST_CODE -> startMainActivity(this)
        }
    }

    fun openOverlayDrawSetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startInstalledAppDetailsActivity(this)
        } else {
            startOverlayPermissionActivity(this)
        }
    }

    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(R.layout.fragment_onboarding, container, false)
            val textView = rootView.findViewById(R.id.section_label) as TextView
            textView.text = getPageText(arguments.getInt(ARG_SECTION_NUMBER))
            return rootView
        }

        fun getPageText(position: Int): String = when (position) {
            0 -> "To record audio and video of Pokemon we'll need access to your external storage and mic."
            1 -> "Select \"Do Not Show Again\" to allow us to start and stop recording automatically when you encounter a pokemon."
            2 -> {
                val message = "To log additional information about your Pokemon captures you'll need to grant us permission to \"Draw over other apps\""
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) message + "at the bottom of the list."
                else message + "."
            }
            3 -> "We need permission in order to determine if Pokemon Go is running on your device. Toggle \"Permit usage access\" on for this application."
            else -> ""
        }

        companion object {

            private val ARG_SECTION_NUMBER = "section_number"

            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position)
        }

        override fun getCount(): Int {
            return 4
        }
    }
}

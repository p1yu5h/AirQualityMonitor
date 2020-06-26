package com.piyushsatija.pollutionmonitor.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.utils.GPSUtils
import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils
import com.piyushsatija.pollutionmonitor.view.fragment.AQIFragment

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val logTag = javaClass.simpleName
    private var sharedPrefUtils: SharedPrefUtils? = null
    private lateinit var aqiFragment: AQIFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            sharedPrefUtils = SharedPrefUtils.getInstance(this)
            if (sharedPrefUtils?.appInstallTime == 0L) sharedPrefUtils?.appInstallTime = System.currentTimeMillis()
            if (sharedPrefUtils?.isDarkMode == true) setTheme(R.style.AppTheme_Dark) else setTheme(R.style.AppTheme_Light)

            setContentView(R.layout.activity_main)
            FirebaseMessaging.getInstance().subscribeToTopic("weather")
                    .addOnCompleteListener { Log.d("FCM", "Subscribed to \"weather\"") }

            aqiFragment = AQIFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, aqiFragment)
            transaction.commit()
        } catch (e: Exception) {
            Log.e(logTag, e.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPSUtils.GPS_REQUEST) {
            if (::aqiFragment.isInitialized) {
                aqiFragment.locationPermissionResult(success = resultCode == Activity.RESULT_OK)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            GPSUtils.MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity,
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (::aqiFragment.isInitialized) {
                            aqiFragment.locationPermissionResult(success = true)
                        }
                    }
                } else {
                    if (::aqiFragment.isInitialized) {
                        aqiFragment.locationPermissionResult(success = false)
                    }
                }
            }
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
        }
    }
}
package com.piyushsatija.pollutionmonitor.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.utils.GPSUtils
import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils
import com.piyushsatija.pollutionmonitor.view.fragment.AQIFragment
import com.piyushsatija.pollutionmonitor.view.fragment.SearchFragment
import com.piyushsatija.pollutionmonitor.view.fragment.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, MessageInterface {
    private val logTag = javaClass.simpleName
    private var sharedPrefUtils: SharedPrefUtils? = null
    private var aqiFragment = AQIFragment()
    private var searchFragment = SearchFragment()
    private var settingsFragment = SettingsFragment()
    private val fm = supportFragmentManager
    private var currentFragment: Fragment = aqiFragment
    var updateValues = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            sharedPrefUtils = SharedPrefUtils.getInstance(this)
            if (sharedPrefUtils?.appInstallTime == 0L) sharedPrefUtils?.appInstallTime = System.currentTimeMillis()
            setTheme(R.style.AppTheme_Light)
            setContentView(R.layout.activity_main)
            FirebaseMessaging.getInstance().subscribeToTopic("weather")
                    .addOnCompleteListener { Log.d("FCM", "Subscribed to \"weather\"") }

            fm.beginTransaction().add(R.id.fragmentContainer, settingsFragment, "settings").hide(settingsFragment).commit()
            fm.beginTransaction().add(R.id.fragmentContainer, searchFragment, "search").hide(searchFragment).commit()
            fm.beginTransaction().add(R.id.fragmentContainer, aqiFragment, "aqi").commit()

            bottomNavigation.setOnNavigationItemSelectedListener(this)
        } catch (e: Exception) {
            Log.e(logTag, e.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPSUtils.GPS_REQUEST) {
            aqiFragment.locationPermissionResult(success = resultCode == Activity.RESULT_OK)
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
                        aqiFragment.locationPermissionResult(success = true)
                    }
                } else {
                    aqiFragment.locationPermissionResult(success = false)
                }
            }
        }
    }

    override fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(fragmentContainer, message, Snackbar.LENGTH_LONG)
        snackbar.anchorView = bottomNavigation
        snackbar.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.aqiNav -> {
                fm.beginTransaction().hide(currentFragment).show(aqiFragment).commit()
                if (updateValues) aqiFragment.updateValues()
                currentFragment = aqiFragment
                return true
            }
            R.id.searchNav -> {
                fm.beginTransaction().hide(currentFragment).show(searchFragment).commit()
                searchFragment.clearResults(fromBottomNav = true)
                currentFragment = searchFragment
                return true
            }
            R.id.settingsNav -> {
                fm.beginTransaction().hide(currentFragment).show(settingsFragment).commit()
                currentFragment = settingsFragment
                return true
            }
            else -> false
        }
    }

    override fun onBackPressed() {
        if (currentFragment != aqiFragment) {
            fm.beginTransaction().hide(currentFragment).show(aqiFragment).commit()
            if (updateValues) aqiFragment.updateValues()
            currentFragment = aqiFragment
        } else {
            finish()
        }
    }
}
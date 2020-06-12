package com.piyushsatija.pollutionmonitor.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.piyushsatija.pollutionmonitor.BuildConfig
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils
import com.piyushsatija.pollutionmonitor.view.MainActivity

class SplashActivity : AppCompatActivity() {
    private val handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPrefUtils = SharedPrefUtils.getInstance(this)
        if (sharedPrefUtils!!.isDarkMode) setTheme(R.style.AppTheme_Dark) else setTheme(R.style.AppTheme_Light)
        setContentView(R.layout.activity_splash)
        val versionTextView = findViewById<TextView>(R.id.splash_version_text)
        versionTextView.text = String.format("v%s", BuildConfig.VERSION_NAME)
        handler.postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        //Remove all the callbacks otherwise navigation will execute even after activity is killed or closed.
        handler.removeCallbacksAndMessages(null)
    }
}
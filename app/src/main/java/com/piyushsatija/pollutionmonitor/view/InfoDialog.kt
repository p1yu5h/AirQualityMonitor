package com.piyushsatija.pollutionmonitor.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.model.PollutionLevels

class InfoDialog internal constructor(private val context1: Context, private val pollutionLevel: PollutionLevels) : Dialog(context1) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_info)
        val window = window
        if (window != null) {
            window.attributes.windowAnimations = R.style.DialogAnimation
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
        setupViews()
    }

    private fun setupViews() {
        val aqiRangeTextView = findViewById<TextView>(R.id.aqi_range)
        val pollutionLevelTextView = findViewById<TextView>(R.id.pollution_level)
        val healthImplicationTextView = findViewById<TextView>(R.id.health_implications)
        val color: Int
        when (pollutionLevel) {
            PollutionLevels.GOOD -> {
                color = ContextCompat.getColor(context1, R.color.scaleGood)
                aqiRangeTextView.setText(R.string.good_range)
                aqiRangeTextView.setTextColor(color)
                pollutionLevelTextView.setBackgroundColor(color)
                pollutionLevelTextView.setText(R.string.good)
                healthImplicationTextView.setText(R.string.good_health_implications)
            }
            PollutionLevels.MODERATE -> {
                color = ContextCompat.getColor(context1, R.color.scaleModerate)
                aqiRangeTextView.setText(R.string.moderate_range)
                aqiRangeTextView.setTextColor(color)
                pollutionLevelTextView.setBackgroundColor(color)
                pollutionLevelTextView.setText(R.string.moderate)
                healthImplicationTextView.setText(R.string.moderate_health_implications)
            }
            PollutionLevels.UNHEALTHY_FOR_SENSITIVE -> {
                color = ContextCompat.getColor(context1, R.color.scaleUnhealthySensitive)
                aqiRangeTextView.setText(R.string.unhealthy_for_sensitive_range)
                aqiRangeTextView.setTextColor(color)
                pollutionLevelTextView.setBackgroundColor(color)
                pollutionLevelTextView.setText(R.string.unhealthy_for_sensitive)
                healthImplicationTextView.setText(R.string.unhealthy_for_sensitive_health_implications)
            }
            PollutionLevels.UNHEALTHY -> {
                color = ContextCompat.getColor(context1, R.color.scaleUnhealthy)
                aqiRangeTextView.setText(R.string.unhealthy_range)
                aqiRangeTextView.setTextColor(color)
                pollutionLevelTextView.setBackgroundColor(color)
                pollutionLevelTextView.setText(R.string.unhealthy)
                healthImplicationTextView.setText(R.string.unhealthy_health_implications)
            }
            PollutionLevels.VERY_UNHEALTHY -> {
                color = ContextCompat.getColor(context1, R.color.scaleVeryUnhealthy)
                aqiRangeTextView.setText(R.string.very_unhealthy_range)
                aqiRangeTextView.setTextColor(color)
                pollutionLevelTextView.setBackgroundColor(color)
                pollutionLevelTextView.setText(R.string.very_unhealthy)
                healthImplicationTextView.setText(R.string.very_unhealthy_health_implications)
            }
            PollutionLevels.HAZARDOUS -> {
                color = ContextCompat.getColor(context1, R.color.scaleHazardous)
                aqiRangeTextView.setText(R.string.hazardous_range)
                aqiRangeTextView.setTextColor(color)
                pollutionLevelTextView.setBackgroundColor(color)
                pollutionLevelTextView.setText(R.string.hazardous)
                healthImplicationTextView.setText(R.string.hazardous_health_implications)
            }
        }
    }

}
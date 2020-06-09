package com.piyushsatija.pollutionmonitor

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils
import com.piyushsatija.pollutionmonitor.view.MainActivity

/**
 * Implementation of App Widget functionality.
 */
class AQIWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        WorkManager.getInstance().cancelAllWork()
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                            appWidgetId: Int) {

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_aqi)
            val aqiString = SharedPrefUtils.getInstance(context)?.latestAQI
            views.setTextViewText(R.id.widget_aqi_text, aqiString)
            var airQuality = ""
            val aqi = aqiString?.toInt() ?: 0
            var colorId =
            when {
                aqi in 0..50 -> {
                    airQuality = context.getString(R.string.good)
                    ContextCompat.getColor(context, R.color.scaleGood)
                }
                aqi in 51..100 -> {
                    airQuality = context.getString(R.string.moderate)
                    ContextCompat.getColor(context, R.color.scaleModerate)
                }
                aqi in 101..150 -> {
                    airQuality = context.getString(R.string.unhealthy)
                    ContextCompat.getColor(context, R.color.scaleUnhealthySensitive)
                }
                aqi in 151..200 -> {
                    airQuality = context.getString(R.string.unhealthy)
                    ContextCompat.getColor(context, R.color.scaleUnhealthy)
                }
                aqi in 201..300 -> {
                    airQuality = context.getString(R.string.very_unhealthy)
                    ContextCompat.getColor(context, R.color.scaleVeryUnhealthy)
                }
                aqi >= 301 -> {
                    airQuality = context.getString(R.string.hazardous)
                    ContextCompat.getColor(context, R.color.scaleHazardous)
                }
                else -> {
                    ContextCompat.getColor(context, R.color.scaleGood)
                }
            }
            views.setTextViewText(R.id.widget_air_quality_text, airQuality)
            views.setTextColor(R.id.widget_air_quality_text, ContextCompat.getColor(context, R.color.grey))
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            views.setOnClickPendingIntent(R.id.widget_background, pendingIntent)
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
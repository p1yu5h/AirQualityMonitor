package com.piyushsatija.pollutionmonitor;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.work.WorkManager;

import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils;
import com.piyushsatija.pollutionmonitor.view.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class AQIWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_aqi);
        String aqiString = SharedPrefUtils.getInstance(context).getLatestAQI();
        views.setTextViewText(R.id.widget_aqi_text, aqiString);
        String airQuality = "";
        int aqi = Integer.parseInt(aqiString);
        int colorId = -1;
        if (aqi >= 0 && aqi <= 50) {
            airQuality = context.getString(R.string.good);
            colorId = context.getResources().getColor(R.color.scaleGood);
        } else if (aqi >= 51 && aqi <= 100) {
            airQuality = context.getString(R.string.moderate);
            colorId = context.getResources().getColor(R.color.scaleModerate);
        } else if (aqi >= 101 && aqi <= 150) {
            airQuality = context.getString(R.string.unhealthy);
            colorId = context.getResources().getColor(R.color.scaleUnhealthySensitive);
        } else if (aqi >= 151 && aqi <= 200) {
            airQuality = context.getString(R.string.unhealthy);
            colorId = context.getResources().getColor(R.color.scaleUnhealthy);
        } else if (aqi >= 201 && aqi <= 300) {
            airQuality = context.getString(R.string.very_unhealthy);
            colorId = context.getResources().getColor(R.color.scaleVeryUnhealthy);
        } else if (aqi >= 301) {
            airQuality = context.getString(R.string.hazardous);
            colorId = context.getResources().getColor(R.color.scaleHazardous);
        }
        views.setTextViewText(R.id.widget_air_quality_text, airQuality);
        views.setTextColor(R.id.widget_air_quality_text, context.getResources().getColor(R.color.grey));

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_aqi_text, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        WorkManager.getInstance().cancelAllWork();
    }
}


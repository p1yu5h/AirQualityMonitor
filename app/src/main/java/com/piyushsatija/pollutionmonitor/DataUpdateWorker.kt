package com.piyushsatija.pollutionmonitor

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.piyushsatija.pollutionmonitor.api.RetrofitHelper.Companion.instance
import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils
import java.io.IOException

class DataUpdateWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val sharedPrefUtils: SharedPrefUtils? = SharedPrefUtils.getInstance(context)
    override fun doWork(): Result {
        val apiResponseCall = instance?.apiInterface?.getAQI(BuildConfig.ApiKey)
        try {
            val response = apiResponseCall?.execute()
            if (response != null) {
                return if (response.code() == 200 && response.body() != null) {
                    val apiResponse = response.body()
                    val data = apiResponse!!.data
                    if (data != null) {
                        sharedPrefUtils?.saveLatestAQI(data.aqi.toString())
                    }
                    updateWidget()
                    Log.d("Worker", "Work is Done")
                    Result.success()
                } else {
                    Result.failure()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Result.failure()
    }

    private fun updateWidget() {
        val intent = Intent(context, AQIWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, AQIWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }

}
package com.piyushsatija.pollutionmonitor;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class DataUpdateWorker extends Worker {
    private SharedPrefUtils sharedPrefUtils;
    private Context context;

    public DataUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        sharedPrefUtils = SharedPrefUtils.getInstance(context);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Call<ApiResponse> apiResponseCall = RetrofitHelper.getInstance().getApiInterface().getAQI(BuildConfig.ApiKey);
        try {
            Response<ApiResponse> response = apiResponseCall.execute();
            if (response.code() == 200 && response.body() != null) {
                ApiResponse apiResponse = response.body();
                Data data = apiResponse.getData();
                sharedPrefUtils.saveLatestAQI(String.valueOf(data.getAqi()));
                updateWidget();
                Log.d("Worker", "Work is Done");
                return Result.success();
            } else {
                return Result.failure();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.failure();
    }

    private void updateWidget() {
        Intent intent = new Intent(context, AQIWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, AQIWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}

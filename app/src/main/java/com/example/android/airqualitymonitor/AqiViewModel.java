package com.example.android.airqualitymonitor;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AqiViewModel extends ViewModel {
    private RetrofitHelper mRetrofitHelper;
    private ApiInterface mApiInterface;
    private MutableLiveData<ApiResponse> mApiResponse;
    private MutableLiveData<String> mStatus = new MutableLiveData<>();

    public AqiViewModel() {
        super();
        mRetrofitHelper = RetrofitHelper.getInstance();
    }


    public LiveData<ApiResponse> getApiResponse() {
        if (mApiResponse == null) {
            mApiResponse = new MutableLiveData<>();
            loadApiResponse();
        }
        return mApiResponse;
    }

    private void loadApiResponse() {
        mApiInterface = mRetrofitHelper.getApiInterface();
        mStatus.setValue("Fetching data...");
        Call<ApiResponse> mApiResponseCall = mApiInterface.getAQI("demo");
        mApiResponseCall.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                if (response.body() == null) {
                    return;
                }
                
                mApiResponse.setValue(response.body());
                mStatus.setValue("Done");
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.d("Error", "erorr");
            }
        });
    }

    public LiveData<String> getStatus() {
        return mStatus;
    }
}

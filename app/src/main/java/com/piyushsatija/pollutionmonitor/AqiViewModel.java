package com.piyushsatija.pollutionmonitor;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AqiViewModel extends ViewModel {
    private RetrofitHelper mRetrofitHelper;
    private ApiInterface mApiInterface;
    private MutableLiveData<ApiResponse> mApiResponse;
    private MutableLiveData<Status> mStatus = new MutableLiveData<>();
    private final String apiKey = BuildConfig.ApiKey;

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
        mStatus.setValue(Status.FETCHING);
        Call<ApiResponse> mApiResponseCall = mApiInterface.getAQI(apiKey);
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
                mStatus.setValue(Status.DONE);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Log.d("Error", "error");
            }
        });
    }

    public LiveData<ApiResponse> getGPSApiResponse(String geo) {
        if (mApiResponse == null) {
            mApiResponse = new MutableLiveData<>();
            loadGPSBasedApiResponse(geo);
        }
        return mApiResponse;
    }

    private void loadGPSBasedApiResponse(String geo) {
        mApiInterface = mRetrofitHelper.getApiInterface();
        mStatus.setValue(Status.FETCHING);
        Call<ApiResponse> mApiResponseCall = mApiInterface.getLocationAQI(geo, apiKey);
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
                mStatus.setValue(Status.DONE);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Log.d("Error", "error");
            }
        });
    }

    public LiveData<Status> getStatus() {
        return mStatus;
    }
}

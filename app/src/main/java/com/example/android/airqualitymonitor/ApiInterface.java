package com.example.android.airqualitymonitor;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("feed/here/")
    Call<ApiResponse> getAQI(@Query("token") String token);
}

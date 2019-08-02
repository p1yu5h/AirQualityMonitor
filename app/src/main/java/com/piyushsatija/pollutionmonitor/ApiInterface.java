package com.piyushsatija.pollutionmonitor;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("feed/here/")
    Call<ApiResponse> getAQI(@Query("token") String token);

    @GET("feed/{geo}/")
    Call<ApiResponse> getLocationAQI(@Path("geo") String geo, @Query("token") String token);
}

package com.piyushsatija.pollutionmonitor

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {
    @GET("feed/here/")
    fun getAQI(@Query("token") token: String?): Call<ApiResponse?>?

    @GET("feed/{geo}/")
    fun getLocationAQI(@Path("geo") geo: String?, @Query("token") token: String?): Call<ApiResponse?>?
}
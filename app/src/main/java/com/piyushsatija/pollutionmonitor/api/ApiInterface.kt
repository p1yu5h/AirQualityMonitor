package com.piyushsatija.pollutionmonitor.api

import com.piyushsatija.pollutionmonitor.api.ApiResponse
import com.piyushsatija.pollutionmonitor.api.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {
    @GET("feed/here/")
    fun getAQI(@Query("token") token: String?): Call<ApiResponse?>?

    @GET("feed/{geo}/")
    fun getLocationAQI(@Path("geo") geo: String?, @Query("token") token: String?): Call<ApiResponse?>?

    @GET("search/")
    fun searchAQI(@Query("keyword") keyword: String?, @Query("token") token: String?): Call<SearchResponse?>?
}
package com.piyushsatija.pollutionmonitor

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.piyushsatija.pollutionmonitor.api.RetrofitHelper.Companion.instance
import com.piyushsatija.pollutionmonitor.api.ApiInterface
import com.piyushsatija.pollutionmonitor.api.ApiResponse
import com.piyushsatija.pollutionmonitor.api.RetrofitHelper
import com.piyushsatija.pollutionmonitor.api.SearchResponse
import com.piyushsatija.pollutionmonitor.model.Status
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AqiViewModel : ViewModel() {
    private val logTag = javaClass.simpleName
    private val mRetrofitHelper: RetrofitHelper? = instance
    private var mApiInterface: ApiInterface? = null
    private var mApiResponse: MutableLiveData<ApiResponse?>? = null
    private val mStatus = MutableLiveData<Status>()
    private val apiKey = BuildConfig.ApiKey
    val apiResponse: LiveData<ApiResponse?>
        get() {
            if (mApiResponse == null) {
                mApiResponse = MutableLiveData()
                loadApiResponse()
            }
            return mApiResponse!!
        }

    private fun loadApiResponse() {
        mApiInterface = mRetrofitHelper?.apiInterface
        mStatus.value = Status.FETCHING
        val mApiResponseCall = mApiInterface?.getAQI(apiKey)
        mApiResponseCall?.enqueue(object : Callback<ApiResponse?> {
            override fun onResponse(call: Call<ApiResponse?>, response: Response<ApiResponse?>) {
                if (!response.isSuccessful) {
                    return
                }
                if (response.body() == null) {
                    return
                }
                mApiResponse?.value = response.body()
                mStatus.value = Status.DONE
            }

            override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                Log.e(logTag, "loadApiResponse", t)
            }
        })
    }

    fun searchKeyword(keyword: String) {
        mApiInterface = mRetrofitHelper?.apiInterface
        mStatus.value = Status.FETCHING
        val mApiResponseCall = mApiInterface?.searchAQI(keyword, apiKey)
        mApiResponseCall?.enqueue(object : Callback<SearchResponse?> {
            override fun onResponse(call: Call<SearchResponse?>, response: Response<SearchResponse?>) {
                if (!response.isSuccessful) {
                    return
                }
                if (response.body() == null) {
                    return
                }
                mStatus.value = Status.DONE
            }

            override fun onFailure(call: Call<SearchResponse?>, t: Throwable) {
                Log.e(logTag, "searchKeyword", t)
            }
        })
    }

    fun getGPSApiResponse(geo: String): LiveData<ApiResponse?> {
        if (mApiResponse == null) {
            mApiResponse = MutableLiveData()
            loadGPSBasedApiResponse(geo)
        }
        return mApiResponse!!
    }

    private fun loadGPSBasedApiResponse(geo: String) {
        mApiInterface = mRetrofitHelper?.apiInterface
        mStatus.value = Status.FETCHING
        val mApiResponseCall = mApiInterface?.getLocationAQI(geo, apiKey)
        mApiResponseCall!!.enqueue(object : Callback<ApiResponse?> {
            override fun onResponse(call: Call<ApiResponse?>, response: Response<ApiResponse?>) {
                if (!response.isSuccessful) {
                    return
                }
                if (response.body() == null) {
                    return
                }
                mApiResponse?.value = response.body()
                mStatus.value = Status.DONE
            }

            override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                Log.e(logTag, "loadGPSBasedApiResponse", t)
            }
        })
    }

    val status: LiveData<Status>
        get() = mStatus

}
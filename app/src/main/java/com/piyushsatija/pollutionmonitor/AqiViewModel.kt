package com.piyushsatija.pollutionmonitor

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.piyushsatija.pollutionmonitor.RetrofitHelper.Companion.instance
import com.piyushsatija.pollutionmonitor.model.Status
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AqiViewModel : ViewModel() {
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
                Log.d("Error", "error")
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
                Log.d("Error", "error")
            }
        })
    }

    val status: LiveData<Status>
        get() = mStatus

}
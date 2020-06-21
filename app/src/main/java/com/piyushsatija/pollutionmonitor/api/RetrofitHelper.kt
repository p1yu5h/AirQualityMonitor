package com.piyushsatija.pollutionmonitor.api

import android.app.ProgressDialog
import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitHelper private constructor() {
    private var retrofit: Retrofit? = null
    val apiInterface: ApiInterface?
    private var mProgressDialog: ProgressDialog? = null

    fun showProgressDialog(context: Context?, Message: String?) {
        dismissProgressDialog()
        mProgressDialog = ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT)
        mProgressDialog?.setCancelable(false)
        mProgressDialog?.setCanceledOnTouchOutside(false)
        mProgressDialog?.setMessage(Message)
        mProgressDialog?.show()
    }

    fun dismissProgressDialog() {
        if (mProgressDialog?.isShowing == true) {
            mProgressDialog?.dismiss()
        }
    }

    companion object {
        private var sRetrofitHelper: RetrofitHelper? = null
        private const val BASE_URL = "https://api.waqi.info/"
        @JvmStatic
        val instance: RetrofitHelper?
            get() {
                if (sRetrofitHelper == null) {
                    sRetrofitHelper = RetrofitHelper()
                }
                return sRetrofitHelper
            }
    }

    init {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
        apiInterface = retrofit?.create(ApiInterface::class.java)
    }
}
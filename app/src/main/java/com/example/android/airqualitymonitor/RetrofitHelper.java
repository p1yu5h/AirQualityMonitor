package com.example.android.airqualitymonitor;

import android.app.ProgressDialog;
import android.content.Context;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {
    private static RetrofitHelper sRetrofitHelper;
    private Retrofit retrofit;
    private final ApiInterface mApiInterface;
    private ProgressDialog mProgressDialog;

    private static final String BASE_URL = "https://api.waqi.info/";

    private RetrofitHelper() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        mApiInterface = retrofit.create(ApiInterface.class);
    }

    public static RetrofitHelper getInstance() {
        if (sRetrofitHelper == null) {
            sRetrofitHelper = new RetrofitHelper();
        }
        return sRetrofitHelper;
    }

    public ApiInterface getApiInterface() {
        return mApiInterface;
    }

    public void showProgressDialog(Context context, String Message) {
        dismissProgressDialog();
        mProgressDialog = new ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage(Message);
        mProgressDialog.show();
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}

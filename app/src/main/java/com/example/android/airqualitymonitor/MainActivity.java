package com.example.android.airqualitymonitor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    Button button;
    private TextView mAqiText;
    //TODO enter your api key here
    public static String apiKey = "demo";
    ApiInterface mApiInterface;
    String mToken;
    ApiResponse mResponse;
    Retrofit retrofit;
    public static final String BASE_URL = "https://api.waqi.info/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        mAqiText = findViewById(R.id.aqitext);

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mApiInterface = retrofit.create(ApiInterface.class);
                mToken = apiKey;
                Call<ApiResponse> mApiResponseCall = mApiInterface.getAQI(mToken);
                mApiResponseCall.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (!response.isSuccessful()) {
                            return;
                        }

                        if (response.body() == null) {
                            return;
                        }

                        mResponse = response.body();
                        mAqiText.setText(String.valueOf(mResponse.getData().getAqi()));
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Log.d("Error", "erorr");
                    }
                });
            }
        });


    }
}

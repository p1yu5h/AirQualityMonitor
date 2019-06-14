package com.example.android.airqualitymonitor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private TextView mAqiText;
    AqiViewModel mAqiViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAqiText = findViewById(R.id.aqitext);
        mAqiViewModel = ViewModelProviders.of(this).get(AqiViewModel.class);
        mAqiViewModel.getStatus().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null) {
                    if (s.equals("Fetching data...")) {
                        showDialog(s);
                    } else dismissDialog();
                }
            }
        });
        mAqiViewModel.getApiResponse().observe(this, new Observer<ApiResponse>() {
            @Override
            public void onChanged(ApiResponse apiResponse) {
                if (apiResponse != null) {
                    mAqiText.setText(String.valueOf(apiResponse.getData().getAqi()));
                }
            }
        });
    }

    private void showDialog(String s) {
        RetrofitHelper.getInstance().showProgressDialog(this, s);
    }

    private void dismissDialog() {
        RetrofitHelper.getInstance().dismissProgressDialog();
    }
}

package com.example.android.airqualitymonitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.airqualitymonitor.Adapters.PollutantsAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    //Views
    private TextView aqiTextView, temperatureTextView, locationTextView, pressureTextView, humidityTextView, windTextView;
    private RecyclerView pollutantsRecyclerview;

    //Data
    private AqiViewModel aqiViewModel;
    private Data data = new Data();
    private PollutantsAdapter pollutantsAdapter;
    private List<Pollutant> pollutantsList = new ArrayList<>();
    private Call<ApiResponse> loadAQICall;
    private RetrofitHelper retrofitHelper;
    private ApiInterface apiInterface;

    FusedLocationProviderClient fusedLocationClient;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Location lastLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        aqiViewModel = ViewModelProviders.of(this).get(AqiViewModel.class);
        retrofitHelper = RetrofitHelper.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    lastLocation = location;
                    loadAQIData(lastLocation.getLatitude(), lastLocation.getLongitude());
                });

            }
        }

        /*aqiViewModel.getStatus().observe(this, s -> {
            if (s != null) {
                if (s.equals("Fetching data...")) {
                    showDialog(s);
                } else dismissDialog();
            }
        });
        aqiViewModel.getApiResponse().observe(this, apiResponse -> {
            if (apiResponse != null) {
                data = apiResponse.getData();
                aqiTextView.setText(String.valueOf(data.getAqi()));
                setAqiScaleGroup();
                Iaqi iaqi = data.getIaqi();
                if (iaqi.getTemperature() != null) temperatureTextView.setText(getString(R.string.temperature_unit_celsius, data.getIaqi().getTemperature().getV()));
                if (iaqi.getPressure() != null) pressureTextView.setText(getString(R.string.pressure_unit, iaqi.getPressure().getV()));
                if (iaqi.getHumidity() != null) humidityTextView.setText(getString(R.string.humidity_unit, iaqi.getHumidity().getV()));
                if (iaqi.getWind() != null) windTextView.setText(getString(R.string.wind_unit, iaqi.getWind().getV()));
                locationTextView.setText(data.getCity().getName());
                setupRecyclerView();
                addPollutantsToList(data.getIaqi());
            }
        });*/
    }

    private void init() {
        aqiTextView = findViewById(R.id.aqi_textview);
        temperatureTextView = findViewById(R.id.temperature_textview);
        locationTextView = findViewById(R.id.location_textview);
        pressureTextView = findViewById(R.id.pressure_textview);
        humidityTextView = findViewById(R.id.humidity_textview);
        windTextView = findViewById(R.id.wind_textview);
    }

    private void setupRecyclerView() {
        pollutantsRecyclerview = findViewById(R.id.pollutants_recyclerview);
        pollutantsRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        pollutantsRecyclerview.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(pollutantsRecyclerview.getContext(),
                DividerItemDecoration.VERTICAL);
        pollutantsRecyclerview.addItemDecoration(dividerItemDecoration);
    }

    private void addPollutantsToList(Iaqi iaqi) {
        pollutantsList.add(new Pollutant("Carbon Monoxide", iaqi.getCo().getV()));
        pollutantsList.add(new Pollutant("Nitrous Dioxide", iaqi.getNo2().getV()));
        pollutantsList.add(new Pollutant("Ozone", iaqi.getO3().getV()));
        pollutantsList.add(new Pollutant("PM 2.5", iaqi.getPm2_5().getV()));
        pollutantsList.add(new Pollutant("PM 10", iaqi.getPm10().getV()));
        pollutantsList.add(new Pollutant("Sulfur Dioxide", iaqi.getSo2().getV()));
        pollutantsAdapter = new PollutantsAdapter(pollutantsList);
        pollutantsRecyclerview.setAdapter(pollutantsAdapter);
    }

    private void setAqiScaleGroup() {
        int aqi = data.getAqi();
        TextView aqiScaleText;
        if (aqi >= 0 && aqi <= 50) aqiScaleText = findViewById(R.id.scaleGood);
        else if (aqi >= 51 && aqi <= 100) aqiScaleText = findViewById(R.id.scaleModerate);
        else if (aqi >= 101 && aqi <= 150)
            aqiScaleText = findViewById(R.id.scaleUnhealthySensitive);
        else if (aqi >= 151 && aqi <= 200) aqiScaleText = findViewById(R.id.scaleUnhealthy);
        else if (aqi >= 201 && aqi <= 300) aqiScaleText = findViewById(R.id.scaleVeryUnhealthy);
        else if (aqi >= 301) aqiScaleText = findViewById(R.id.scaleVeryUnhealthy);
        else aqiScaleText = findViewById(R.id.scaleGood);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            aqiScaleText.setForeground(getDrawable(R.drawable.selected_aqi_foreground));
        }
    }

    private void showDialog(String s) {
        RetrofitHelper.getInstance().showProgressDialog(this, s);
    }

    private void dismissDialog() {
        RetrofitHelper.getInstance().dismissProgressDialog();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Allow Location Access")
                        .setMessage("Allow Location Access")
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> lastLocation = location);
                        loadAQIData(lastLocation.getLatitude(), lastLocation.getLongitude());
                    }

                } else {
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getAqiData() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        String geo = "geo:" + location.getLatitude() + ";" + location.getLongitude();
                        aqiViewModel.getStatus().observe(MainActivity.this, s -> {
                            if (s != null) {
                                if (s.equals("Fetching data...")) {
                                    showDialog(s);
                                } else dismissDialog();
                            }
                        });
                        aqiViewModel.getGPSApiResponse(geo).observe(MainActivity.this, apiResponse -> {
                            if (apiResponse != null) {
                                Log.d("api", String.valueOf(apiResponse));
                                data = apiResponse.getData();
                                aqiTextView.setText(String.valueOf(data.getAqi()));
                                setAqiScaleGroup();
                                Iaqi iaqi = data.getIaqi();
                                if (iaqi.getTemperature() != null)
                                    temperatureTextView.setText(getString(R.string.temperature_unit_celsius, data.getIaqi().getTemperature().getV()));
                                if (iaqi.getPressure() != null)
                                    pressureTextView.setText(getString(R.string.pressure_unit, iaqi.getPressure().getV()));
                                if (iaqi.getHumidity() != null)
                                    humidityTextView.setText(getString(R.string.humidity_unit, iaqi.getHumidity().getV()));
                                if (iaqi.getWind() != null)
                                    windTextView.setText(getString(R.string.wind_unit, iaqi.getWind().getV()));
                                locationTextView.setText(data.getCity().getName());
                                setupRecyclerView();
                                addPollutantsToList(data.getIaqi());
                            }
                        });
                    }
                });
    }

    private void loadAQIData(double latitude, double longitude) {
        apiInterface = retrofitHelper.getApiInterface();
        String geo = "geo:" + latitude + ";" + longitude;
        loadAQICall = apiInterface.getLocationAQI(geo, "demo");
        loadAQICall.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                if (response.body() == null) {
                    return;
                }
                data = response.body().getData();
                aqiTextView.setText(String.valueOf(data.getAqi()));
                setAqiScaleGroup();
                Iaqi iaqi = data.getIaqi();
                if (iaqi.getTemperature() != null)
                    temperatureTextView.setText(getString(R.string.temperature_unit_celsius, data.getIaqi().getTemperature().getV()));
                if (iaqi.getPressure() != null)
                    pressureTextView.setText(getString(R.string.pressure_unit, iaqi.getPressure().getV()));
                if (iaqi.getHumidity() != null)
                    humidityTextView.setText(getString(R.string.humidity_unit, iaqi.getHumidity().getV()));
                if (iaqi.getWind() != null)
                    windTextView.setText(getString(R.string.wind_unit, iaqi.getWind().getV()));
                locationTextView.setText(data.getCity().getName());
                setupRecyclerView();
                addPollutantsToList(data.getIaqi());
                Log.d("response", String.valueOf(response.body()));
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, Throwable t) {
                Log.d("Error", "erorr");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lastLocation != null) loadAQIData(lastLocation.getLatitude(), lastLocation.getLongitude());
    }
}

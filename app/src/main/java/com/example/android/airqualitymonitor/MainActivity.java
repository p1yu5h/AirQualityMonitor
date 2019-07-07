package com.example.android.airqualitymonitor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Views
    private TextView aqiTextView, temperatureTextView, locationTextView, pressureTextView, humidityTextView, windTextView;
    private RecyclerView pollutantsRecyclerview;

    //Data
    private AqiViewModel aqiViewModel;
    private Data data = new Data();
    private PollutantsAdapter pollutantsAdapter;
    private List<Pollutant> pollutantsList = new ArrayList<>();

    //Location
    FusedLocationProviderClient fusedLocationClient;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationManager locationManager;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        aqiViewModel = ViewModelProviders.of(this).get(AqiViewModel.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        context = this;
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.
                PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(20 * 1000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        getAqiData(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                    }
                }
            }
        };
    }

    private void init() {
        aqiTextView = findViewById(R.id.aqi_textview);
        temperatureTextView = findViewById(R.id.temperature_textview);
        locationTextView = findViewById(R.id.location_textview);
        pressureTextView = findViewById(R.id.pressure_textview);
        humidityTextView = findViewById(R.id.humidity_textview);
        windTextView = findViewById(R.id.wind_textview);
        setupRecyclerView();
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
        pollutantsList.clear();
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
                        onResume();
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

    private void getAqiData(String latitude, String longitude) {
        String geo = "geo:" + latitude + ";" + longitude;
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
                addPollutantsToList(data.getIaqi());
                pollutantsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                } else {
                    showEnableLocationDialog();
                }
            }
        }
    }

    private void showEnableLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.alert_title_gps_off)
                .setMessage(R.string.alert_content_gps_off)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("CANCEL", ((dialog, which) -> {
                }));
        builder.create().show();
    }
}

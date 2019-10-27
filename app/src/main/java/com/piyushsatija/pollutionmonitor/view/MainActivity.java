package com.piyushsatija.pollutionmonitor.view;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.messaging.FirebaseMessaging;
import com.piyushsatija.pollutionmonitor.AQIWidget;
import com.piyushsatija.pollutionmonitor.AqiViewModel;
import com.piyushsatija.pollutionmonitor.Attribution;
import com.piyushsatija.pollutionmonitor.Data;
import com.piyushsatija.pollutionmonitor.DataUpdateWorker;
import com.piyushsatija.pollutionmonitor.Iaqi;
import com.piyushsatija.pollutionmonitor.Pollutant;
import com.piyushsatija.pollutionmonitor.R;
import com.piyushsatija.pollutionmonitor.RetrofitHelper;
import com.piyushsatija.pollutionmonitor.Status;
import com.piyushsatija.pollutionmonitor.adapters.PollutantsAdapter;
import com.piyushsatija.pollutionmonitor.utils.GPSUtils;
import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.piyushsatija.pollutionmonitor.PollutionLevels.GOOD;
import static com.piyushsatija.pollutionmonitor.PollutionLevels.HAZARDOUS;
import static com.piyushsatija.pollutionmonitor.PollutionLevels.MODERATE;
import static com.piyushsatija.pollutionmonitor.PollutionLevels.UNHEALTHY;
import static com.piyushsatija.pollutionmonitor.PollutionLevels.UNHEALTHY_FOR_SENSITIVE;
import static com.piyushsatija.pollutionmonitor.PollutionLevels.VERY_UNHEALTHY;
import static com.piyushsatija.pollutionmonitor.utils.GPSUtils.GPS_REQUEST;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //Views
    private TextView aqiTextView, temperatureTextView, locationTextView, pressureTextView, humidityTextView, windTextView, attributionTextView;
    private RecyclerView pollutantsRecyclerView;
    private ViewGroup rateUsCard;

    //Data
    private AqiViewModel aqiViewModel;
    private Data data = new Data();
    private PollutantsAdapter pollutantsAdapter;
    private List<Pollutant> pollutantsList = new ArrayList<>();
    private SharedPrefUtils sharedPrefUtils;

    //Location
    private FusedLocationProviderClient fusedLocationClient;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location latestLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        if (sharedPrefUtils.getAppInstallTime() == 0)
            sharedPrefUtils.setAppInstallTime(System.currentTimeMillis());
        if (sharedPrefUtils.isDarkMode()) setTheme(R.style.AppTheme_Dark);
        else setTheme(R.style.AppTheme_Light);
        setContentView(R.layout.activity_main);
        init();
        aqiViewModel = ViewModelProviders.of(this).get(AqiViewModel.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
                        latestLocation = location;
                        getAqiData(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                    }
                }
            }
        };
        checkGPSAndRequestLocation();
        scheduleWidgetUpdater();
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("weather")
                    .addOnCompleteListener(task -> Log.d("FCM", "Subscribed to \"weather\""));
        } catch (Exception e) {
            Log.e("FCM", "Unable to add FCM topic");
        }
    }

    private void checkGPSAndRequestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //Call for AQI data based on location is done in "locationCallback"
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                } else {
                    new GPSUtils(this).turnGPSOn();
                }
            }
        }
    }

    private void scheduleWidgetUpdater() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(DataUpdateWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance().enqueue(periodicWorkRequest);
    }

    private void init() {
        aqiTextView = findViewById(R.id.aqi_text_view);
        temperatureTextView = findViewById(R.id.temperature_text_view);
        locationTextView = findViewById(R.id.location_text_view);
        pressureTextView = findViewById(R.id.pressure_text_view);
        humidityTextView = findViewById(R.id.humidity_text_view);
        windTextView = findViewById(R.id.wind_text_view);
        attributionTextView = findViewById(R.id.attribution_text_view);
        rateUsCard = findViewById(R.id.rateUsLayout);
        setupRecyclerView();
        setupClickListeners();
        setupRateCard();
    }

    private void setupClickListeners() {
        findViewById(R.id.scaleGood).setOnClickListener(this);
        findViewById(R.id.scaleModerate).setOnClickListener(this);
        findViewById(R.id.scaleUnhealthySensitive).setOnClickListener(this);
        findViewById(R.id.scaleUnhealthy).setOnClickListener(this);
        findViewById(R.id.scaleVeryUnhealthy).setOnClickListener(this);
        findViewById(R.id.scaleHazardous).setOnClickListener(this);
        findViewById(R.id.btnDarkMode).setOnClickListener(this);
        findViewById(R.id.btnShare).setOnClickListener(this);
        findViewById(R.id.rateYes).setOnClickListener(this);
        findViewById(R.id.rateNo).setOnClickListener(this);
    }

    private void setupRecyclerView() {
        pollutantsRecyclerView = findViewById(R.id.pollutants_recycler_view);
        pollutantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pollutantsRecyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(pollutantsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        pollutantsRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void addPollutantsToList(Iaqi iaqi) {
        pollutantsList.clear();
        if (iaqi.getCo() != null)
            pollutantsList.add(new Pollutant("Carbon Monoxide - AQI", iaqi.getCo().getV()));
        if (iaqi.getNo2() != null)
            pollutantsList.add(new Pollutant("Nitrous Dioxide - AQI", iaqi.getNo2().getV()));
        if (iaqi.getO3() != null)
            pollutantsList.add(new Pollutant("Ozone - AQI", iaqi.getO3().getV()));
        if (iaqi.getPm2_5() != null)
            pollutantsList.add(new Pollutant("PM 2.5 - AQI", iaqi.getPm2_5().getV()));
        if (iaqi.getPm10() != null)
            pollutantsList.add(new Pollutant("PM 10 - AQI", iaqi.getPm10().getV()));
        if (iaqi.getSo2() != null)
            pollutantsList.add(new Pollutant("Sulfur Dioxide - AQI", iaqi.getSo2().getV()));
        pollutantsAdapter = new PollutantsAdapter(pollutantsList);
        pollutantsRecyclerView.setAdapter(pollutantsAdapter);
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
        else if (aqi >= 301) aqiScaleText = findViewById(R.id.scaleHazardous);
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

    private void checkLocationPermission() {
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
                        .setTitle(R.string.alert_title_location_access)
                        .setMessage(R.string.alert_content_location_access)
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> {
                            getAqiData();
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        checkGPSAndRequestLocation();
                    }

                } else {
                    getAqiData();
                }
            }
        }
    }

    private void getAqiData(String latitude, String longitude) {
        String geo = "geo:" + latitude + ";" + longitude;
        aqiViewModel.getStatus().observe(MainActivity.this, status -> {
            if (status != null) {
                if (status == Status.FETCHING) {
                    showDialog("Getting data from nearest station...");
                } else dismissDialog();
            }
        });
        aqiViewModel.getGPSApiResponse(geo).observe(MainActivity.this, apiResponse -> {
            if (apiResponse != null) {
                Log.d("api", String.valueOf(apiResponse));
                data = apiResponse.getData();
                aqiTextView.setText(String.valueOf(data.getAqi()));
                //TODO: Find better implementation
                sharedPrefUtils.saveLatestAQI(String.valueOf(data.getAqi()));
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
                setupAttributions(data);
                addPollutantsToList(data.getIaqi());
                pollutantsAdapter.notifyDataSetChanged();
                updateWidget();
            }
        });
    }

    private void getAqiData() {
        aqiViewModel.getStatus().observe(MainActivity.this, status -> {
            if (status != null) {
                if (status == Status.FETCHING) {
                    showDialog("Getting data based on network...");
                } else dismissDialog();
            }
        });
        aqiViewModel.getApiResponse().observe(MainActivity.this, apiResponse -> {
            if (apiResponse != null) {
                Log.d("api", String.valueOf(apiResponse));
                data = apiResponse.getData();
                aqiTextView.setText(String.valueOf(data.getAqi()));
                //TODO: Find better implementation
                sharedPrefUtils.saveLatestAQI(String.valueOf(data.getAqi()));
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
                setupAttributions(data);
                addPollutantsToList(data.getIaqi());
                pollutantsAdapter.notifyDataSetChanged();
                updateWidget();
            }
        });
    }

    private void setupAttributions(Data data) {
        int index = 1;
        StringBuilder attributionText = new StringBuilder();
        for (Attribution attribution : data.getAttributions()) {
            attributionText.append(index++)
                    .append(". ")
                    .append(attribution.getName())
                    .append("\n")
                    .append(attribution.getUrl())
                    .append("\n\n");
        }
        attributionTextView.setText(attributionText);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                checkGPSAndRequestLocation();
            } else {
                getAqiData();
            }
        }
    }

    private void updateWidget() {
        Intent intent = new Intent(this, AQIWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), AQIWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    private void shareApp() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareText));
        shareIntent.setType("text/plain");
        startActivity(shareIntent);
    }

    private void setupRateCard() {
        if (!sharedPrefUtils.rateCardDone() && (System.currentTimeMillis() - sharedPrefUtils.getAppInstallTime() >= 86400000)) {
            rateUsCard.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scaleGood:
                new InfoDialog(this, GOOD).show();
                break;
            case R.id.scaleModerate:
                new InfoDialog(this, MODERATE).show();
                break;
            case R.id.scaleUnhealthySensitive:
                new InfoDialog(this, UNHEALTHY_FOR_SENSITIVE).show();
                break;
            case R.id.scaleUnhealthy:
                new InfoDialog(this, UNHEALTHY).show();
                break;
            case R.id.scaleVeryUnhealthy:
                new InfoDialog(this, VERY_UNHEALTHY).show();
                break;
            case R.id.scaleHazardous:
                new InfoDialog(this, HAZARDOUS).show();
                break;
            case R.id.btnDarkMode:
                sharedPrefUtils.isDarkMode(!sharedPrefUtils.isDarkMode());
                recreate();
                break;
            case R.id.btnShare:
                shareApp();
                break;
            case R.id.rateNo:
                rateUsCard.animate().alpha(0.0f).translationX(200f).setDuration(500).withEndAction(() -> {
                    rateUsCard.setVisibility(View.GONE);
                    sharedPrefUtils.rateCardDone(true);
                });
                break;
            case R.id.rateYes:
                sharedPrefUtils.rateCardDone(true);
                rateUsCard.setVisibility(View.GONE);
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.piyushsatija.pollutionmonitor");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                break;
            default:
                break;
        }
    }
}

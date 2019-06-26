package com.example.android.airqualitymonitor;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.airqualitymonitor.Adapters.PollutantsAdapter;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        aqiViewModel = ViewModelProviders.of(this).get(AqiViewModel.class);
        aqiViewModel.getStatus().observe(this, s -> {
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
        });
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
        else if (aqi >= 101 && aqi <= 150) aqiScaleText = findViewById(R.id.scaleUnhealthySensitive);
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
}

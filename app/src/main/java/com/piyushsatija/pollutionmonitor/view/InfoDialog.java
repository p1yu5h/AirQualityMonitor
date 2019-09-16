package com.piyushsatija.pollutionmonitor.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.piyushsatija.pollutionmonitor.PollutionLevels;
import com.piyushsatija.pollutionmonitor.R;

public class InfoDialog extends Dialog {
    private PollutionLevels pollutionLevel;
    private Context context;

    InfoDialog(@NonNull Context context, PollutionLevels pollutionLevel) {
        super(context);
        this.context = context;
        this.pollutionLevel = pollutionLevel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_info);
        Window window = getWindow();
        if (window != null) window.getAttributes().windowAnimations = R.style.DialogAnimation;
        setupViews();
    }


    private void setupViews() {
        TextView aqiRangeTextView = findViewById(R.id.aqi_range);
        TextView pollutionLevelTextView = findViewById(R.id.pollution_level);
        TextView healthImplicationTextView = findViewById(R.id.health_implications);
        int color;
        switch (pollutionLevel) {
            case GOOD:
                color = context.getResources().getColor(R.color.scaleGood);
                aqiRangeTextView.setText(R.string.good_range);
                aqiRangeTextView.setTextColor(color);
                pollutionLevelTextView.setBackgroundColor(color);
                pollutionLevelTextView.setText(R.string.good);
                healthImplicationTextView.setText(R.string.good_health_implications);
                break;
            case MODERATE:
                color = context.getResources().getColor(R.color.scaleModerate);
                aqiRangeTextView.setText(R.string.moderate_range);
                aqiRangeTextView.setTextColor(color);
                pollutionLevelTextView.setBackgroundColor(color);
                pollutionLevelTextView.setText(R.string.moderate);
                healthImplicationTextView.setText(R.string.moderate_health_implications);
                break;
            case UNHEALTHY_FOR_SENSITIVE:
                color = context.getResources().getColor(R.color.scaleUnhealthySensitive);
                aqiRangeTextView.setText(R.string.unhealthy_for_sensitive_range);
                aqiRangeTextView.setTextColor(color);
                pollutionLevelTextView.setBackgroundColor(color);
                pollutionLevelTextView.setText(R.string.unhealthy_for_sensitive);
                healthImplicationTextView.setText(R.string.unhealthy_for_sensitive_health_implications);
                break;
            case UNHEALTHY:
                color = context.getResources().getColor(R.color.scaleUnhealthy);
                aqiRangeTextView.setText(R.string.unhealthy_range);
                aqiRangeTextView.setTextColor(color);
                pollutionLevelTextView.setBackgroundColor(color);
                pollutionLevelTextView.setText(R.string.unhealthy);
                healthImplicationTextView.setText(R.string.unhealthy_health_implications);
                break;
            case VERY_UNHEALTHY:
                color = context.getResources().getColor(R.color.scaleVeryUnhealthy);
                aqiRangeTextView.setText(R.string.very_unhealthy_range);
                aqiRangeTextView.setTextColor(color);
                pollutionLevelTextView.setBackgroundColor(color);
                pollutionLevelTextView.setText(R.string.very_unhealthy);
                healthImplicationTextView.setText(R.string.very_unhealthy_health_implications);
                break;
            case HAZARDOUS:
                color = context.getResources().getColor(R.color.scaleHazardous);
                aqiRangeTextView.setText(R.string.hazardous_range);
                aqiRangeTextView.setTextColor(color);
                pollutionLevelTextView.setBackgroundColor(color);
                pollutionLevelTextView.setText(R.string.hazardous);
                healthImplicationTextView.setText(R.string.hazardous_health_implications);
                break;
        }
    }
}

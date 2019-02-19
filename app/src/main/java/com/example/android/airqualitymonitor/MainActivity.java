package com.example.android.airqualitymonitor;

import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button button;
    public static TextView aqitext;
    //TODO enter your api key here
    public static String apiKey = "demo";
    static ImageView circle;
    static int colorId = R.color.Red;
    static int textColorId = R.color.White;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        aqitext = findViewById(R.id.aqitext);
        circle = findViewById(R.id.circle_imageview);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData process = new fetchData();
                process.execute();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateColor();
    }
        });

    }

    public void updateColor(){
        DrawableCompat.setTint(
               circle.getDrawable(),
               ContextCompat.getColor(this, colorId )
        );
        aqitext.setTextColor(getResources().getColor(textColorId));
    }


}

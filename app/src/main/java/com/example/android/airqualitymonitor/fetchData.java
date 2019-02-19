package com.example.android.airqualitymonitor;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.Integer.parseInt;

public class fetchData extends AsyncTask<Void, Void, Void> {
    String data ="";
    public static String aqi = "";
    public static int aqivalue;


    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL("https://api.waqi.info/feed/here/?token=" + MainActivity.apiKey);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while(line != null){
                line = bufferedReader.readLine();
                data = data + line;
            }

            JSONObject fetchedData = new JSONObject(data);
            JSONObject data = (JSONObject) fetchedData.get("data");
            aqi = data.get("aqi").toString();
            aqivalue = parseInt(aqi);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (aqivalue > 100) {
            MainActivity.colorId = R.color.Red;
            MainActivity.textColorId = R.color.White;
        }else MainActivity.colorId = R.color.Orange;

        MainActivity.aqitext.setText(fetchData.aqi);

    }
}

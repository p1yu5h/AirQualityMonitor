
package com.piyushsatija.airqualitymonitor;

import com.piyushsatija.airqualitymonitor.properties.Co;
import com.piyushsatija.airqualitymonitor.properties.Humidity;
import com.piyushsatija.airqualitymonitor.properties.No2;
import com.piyushsatija.airqualitymonitor.properties.O3;
import com.piyushsatija.airqualitymonitor.properties.Pm10;
import com.piyushsatija.airqualitymonitor.properties.Pm2_5;
import com.piyushsatija.airqualitymonitor.properties.Pressure;
import com.piyushsatija.airqualitymonitor.properties.So2;
import com.piyushsatija.airqualitymonitor.properties.Temperature;
import com.piyushsatija.airqualitymonitor.properties.Wind;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Iaqi {

    @SerializedName("co")
    @Expose
    private Co co;
    @SerializedName("no2")
    @Expose
    private No2 no2;
    @SerializedName("o3")
    @Expose
    private O3 o3;
    @SerializedName("p")
    @Expose
    private Pressure pressure;
    @SerializedName("pm10")
    @Expose
    private Pm10 pm10;
    @SerializedName("pm25")
    @Expose
    private Pm2_5 pm25;
    @SerializedName("so2")
    @Expose
    private So2 so2;
    @SerializedName("t")
    @Expose
    private Temperature temperature;
    @SerializedName("h")
    @Expose
    private Humidity humidity;
    @SerializedName("w")
    @Expose
    private Wind wind;

    public Co getCo() {
        return co;
    }

    public void setCo(Co co) {
        this.co = co;
    }

    public No2 getNo2() {
        return no2;
    }

    public void setNo2(No2 no2) {
        this.no2 = no2;
    }

    public O3 getO3() {
        return o3;
    }

    public void setO3(O3 o3) {
        this.o3 = o3;
    }

    public Pm10 getPm10() {
        return pm10;
    }

    public void setPm10(Pm10 pm10) {
        this.pm10 = pm10;
    }

    public Pm2_5 getPm2_5() {
        return pm25;
    }

    public void setPm2_5(Pm2_5 pm25) {
        this.pm25 = pm25;
    }

    public So2 getSo2() {
        return so2;
    }

    public void setSo2(So2 so2) {
        this.so2 = so2;
    }

    public Pressure getPressure() {
        return pressure;
    }

    public void setPressure(Pressure pressure) {
        this.pressure = pressure;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Humidity getHumidity() {
        return humidity;
    }

    public void setHumidity(Humidity humidity) {
        this.humidity = humidity;
    }

}

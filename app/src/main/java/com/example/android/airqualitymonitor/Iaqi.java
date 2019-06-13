
package com.example.android.airqualitymonitor;

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
    @SerializedName("pm10")
    @Expose
    private Pm10 pm10;
    @SerializedName("pm25")
    @Expose
    private Pm25 pm25;
    @SerializedName("so2")
    @Expose
    private So2 so2;
    @SerializedName("t")
    @Expose
    private T t;
    @SerializedName("w")
    @Expose
    private W w;

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

    public Pm25 getPm25() {
        return pm25;
    }

    public void setPm25(Pm25 pm25) {
        this.pm25 = pm25;
    }

    public So2 getSo2() {
        return so2;
    }

    public void setSo2(So2 so2) {
        this.so2 = so2;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public W getW() {
        return w;
    }

    public void setW(W w) {
        this.w = w;
    }

}

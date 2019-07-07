
package com.example.android.airqualitymonitor.properties;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pm2_5 {

    @SerializedName("v")
    @Expose
    private Double v;

    public Double getV() {
        return v;
    }

    public void setV(Double v) {
        this.v = v;
    }

}


package com.example.android.airqualitymonitor;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class O3 {

    @SerializedName("v")
    @Expose
    private float v;

    public float getV() {
        return v;
    }

    public void setV(float v) {
        this.v = v;
    }

}

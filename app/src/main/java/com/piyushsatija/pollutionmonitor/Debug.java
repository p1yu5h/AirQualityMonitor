
package com.piyushsatija.pollutionmonitor;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Debug {

    @SerializedName("sync")
    @Expose
    private String sync;

    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }

}

package com.piyushsatija.pollutionmonitor.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.piyushsatija.pollutionmonitor.model.search.Data

class SearchResponse {
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("data")
    @Expose
    var data: List<Data>? = null
}
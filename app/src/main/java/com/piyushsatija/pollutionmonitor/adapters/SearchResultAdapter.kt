package com.piyushsatija.pollutionmonitor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.model.search.Data

class SearchResultAdapter(private val searchResults: List<Data>) : RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_result
                        , parent, false)
        return SearchResultViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val searchResult = searchResults[position]
        holder.stationName.text = searchResult.station.name
        holder.stationAQI.text = searchResult.aqi
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stationName: TextView = itemView.findViewById(R.id.stationName)
        val stationAQI: TextView = itemView.findViewById(R.id.stationAQI)
    }
}
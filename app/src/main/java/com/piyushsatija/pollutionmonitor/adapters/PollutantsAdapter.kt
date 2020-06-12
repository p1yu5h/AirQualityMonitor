package com.piyushsatija.pollutionmonitor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.piyushsatija.pollutionmonitor.model.Pollutant
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.adapters.PollutantsAdapter.PollutantViewHolder

class PollutantsAdapter(private val pollutantsList: List<Pollutant>) : RecyclerView.Adapter<PollutantViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollutantViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.pollutant_list_item
                        , parent, false)
        return PollutantViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PollutantViewHolder, position: Int) {
        val pollutant = pollutantsList[position]
        holder.pollutantNameTextView.text = pollutant.pollutantName
        holder.pollutantValueTextView.text = pollutant.pollutantValue.toString()
    }

    override fun getItemCount(): Int {
        return pollutantsList.size
    }

    inner class PollutantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pollutantNameTextView: TextView = itemView.findViewById(R.id.pollutant_name_text_view)
        val pollutantValueTextView: TextView = itemView.findViewById(R.id.pollutant_value_text_view)

    }

}
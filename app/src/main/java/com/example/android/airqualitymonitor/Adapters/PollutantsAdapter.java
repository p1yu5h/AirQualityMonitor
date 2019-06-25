package com.example.android.airqualitymonitor.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.airqualitymonitor.Pollutant;
import com.example.android.airqualitymonitor.R;

import java.util.List;

public class PollutantsAdapter extends RecyclerView.Adapter<PollutantsAdapter.PollutantViewHolder> {
    private List<Pollutant> pollutantsList;

    public PollutantsAdapter(List<Pollutant> pollutantsList) {
        this.pollutantsList = pollutantsList;
    }

    @NonNull
    @Override
    public PollutantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pollutant_list_item
                        ,parent ,false);
        return new PollutantViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PollutantViewHolder holder, int position) {
        Pollutant pollutant = pollutantsList.get(position);
        holder.pollutantNameTextView.setText(pollutant.getPollutantName());
        holder.pollutantValueTextView.setText(String.valueOf(pollutant.getPollutantValue()));
    }

    @Override
    public int getItemCount() {
        return pollutantsList.size();
    }

    class PollutantViewHolder extends RecyclerView.ViewHolder {
        private TextView pollutantNameTextView, pollutantValueTextView;

        public PollutantViewHolder(@NonNull View itemView) {
            super(itemView);
            pollutantNameTextView = itemView.findViewById(R.id.pollutant_name_textview);
            pollutantValueTextView = itemView.findViewById(R.id.pollutant_value_textview);
        }
    }
}

package com.ashwath.zen_core;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<String> sessionList;

    public HistoryAdapter(List<String> sessionList) {
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // The string format from DB is: "📅 2026-02-15 10:30   ⏳ 25 mins"
        // We will split it to make it look nicer
        String rawData = sessionList.get(position);

        try {
            String[] parts = rawData.split("   "); // Split by the 3 spaces
            holder.dateText.setText(parts[0].replace("📅 ", ""));
            holder.durationText.setText(parts[1].replace("⏳ ", "Focused for "));
        } catch (Exception e) {
            // Fallback if format is different
            holder.dateText.setText(rawData);
            holder.durationText.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, durationText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.tv_session_date);
            durationText = itemView.findViewById(R.id.tv_session_duration);
        }
    }
}
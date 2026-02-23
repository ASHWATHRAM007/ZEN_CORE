package com.ashwath.zen_core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FocusAppsAdapter extends RecyclerView.Adapter<FocusAppsAdapter.ViewHolder> {

    private List<ApplicationInfo> apps;
    private PackageManager packageManager;
    private OnAppClickListener listener; // The communicator

    // 1. Define the Interface (The contract)
    public interface OnAppClickListener {
        void onAppClick(String packageName);
    }

    // 2. Update Constructor to accept the Listener
    public FocusAppsAdapter(Context context, List<ApplicationInfo> apps, OnAppClickListener listener) {
        this.apps = apps;
        this.packageManager = context.getPackageManager();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_focus_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationInfo appInfo = apps.get(position);

        holder.icon.setImageDrawable(packageManager.getApplicationIcon(appInfo));
        holder.name.setText(packageManager.getApplicationLabel(appInfo));

        // 3. When clicked, call the Listener (Main Activity)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppClick(appInfo.packageName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.img_focus_icon);
            name = itemView.findViewById(R.id.tv_focus_name);
        }
    }
}
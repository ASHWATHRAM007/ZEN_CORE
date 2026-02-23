package com.ashwath.zen_core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    private List<ApplicationInfo> appList;
    private PackageManager packageManager;
    // This Set remembers which apps are checked
    public static Set<String> selectedApps = new HashSet<>();

    public AppAdapter(List<ApplicationInfo> appList, PackageManager packageManager) {
        this.appList = appList;
        this.packageManager = packageManager;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_selection, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        ApplicationInfo appInfo = appList.get(position);

        // 1. Set the Name and Icon
        holder.appName.setText(packageManager.getApplicationLabel(appInfo));
        holder.appIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo));

        // 2. Handle Checkbox Logic
        String packageName = appInfo.packageName;
        holder.checkBox.setOnCheckedChangeListener(null); // Reset listener to avoid bugs
        holder.checkBox.setChecked(selectedApps.contains(packageName));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedApps.add(packageName);
            } else {
                selectedApps.remove(packageName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        CheckBox checkBox;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.img_app_icon);
            appName = itemView.findViewById(R.id.tv_app_name);
            checkBox = itemView.findViewById(R.id.cb_app_selected);
        }
    }
}
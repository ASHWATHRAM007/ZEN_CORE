package com.ashwath.zen_core;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private static final String PREFS_NAME = "ZenCorePrefs";
    private static final String KEY_WHITELIST = "WhitelistedApps";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        recyclerView = findViewById(R.id.recycler_view_apps);
        Button saveButton = findViewById(R.id.btn_save_selection);

        // 1. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2. Load previously saved apps (so checks stay checked!)
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> savedPackages = prefs.getStringSet(KEY_WHITELIST, new HashSet<>());

        // Pass the saved list to our Adapter
        AppAdapter.selectedApps = new HashSet<>(savedPackages);

        // 3. Get Installed Apps
        List<ApplicationInfo> installedApps = getInstalledApps();

        // 4. Connect Adapter
        adapter = new AppAdapter(installedApps, getPackageManager());
        recyclerView.setAdapter(adapter);

        // 5. SAVE BUTTON LOGIC
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveWhitelist();
            }
        });
    }

    private void saveWhitelist() {
        // Get the list of checked apps from the Adapter
        Set<String> selected = AppAdapter.selectedApps;

        // Save to Phone Memory
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_WHITELIST, selected);
        editor.apply();

        Toast.makeText(this, "Allowed Apps Saved!", Toast.LENGTH_SHORT).show();
        finish(); // Close screen and go back to Timer
    }

    private List<ApplicationInfo> getInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> userApps = new ArrayList<>();

        for (ApplicationInfo app : allApps) {
            // Filter: Show User Apps + Essential System Apps
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || (app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                userApps.add(app);
            }
        }
        return userApps;
    }
}
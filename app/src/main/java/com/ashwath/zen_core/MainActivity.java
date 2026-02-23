package com.ashwath.zen_core;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar; // Added this import
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements FocusAppsAdapter.OnAppClickListener {

    // Slider Variables
    private TextView durationLabel;
    private SeekBar durationSeekBar;
    private long selectedTimeInMillis = 25 * 60 * 1000; // Default 25 mins

    // Normal Variables
    private TextView timerText;
    private DatabaseHelper dbHelper;
    private Button startButton;
    private Button emergencyButton;
    private Button selectAppsButton;
    private RecyclerView allowedAppsRecycler;
    private CountDownTimer countDownTimer;

    // Timer Logic
    private long timeLeftInMillis; // We removed the static assignment here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Connect XML elements
        timerText = findViewById(R.id.tv_timer);
        startButton = findViewById(R.id.btn_start_focus);
        emergencyButton = findViewById(R.id.btn_emergency);
        selectAppsButton = findViewById(R.id.btn_select_apps);
        allowedAppsRecycler = findViewById(R.id.recycler_allowed_apps);
        dbHelper = new DatabaseHelper(this);
        durationLabel = findViewById(R.id.tv_duration_label);
        durationSeekBar = findViewById(R.id.seekbar_duration);

        // Initialize timer text with default 25 mins
        timeLeftInMillis = selectedTimeInMillis;
        updateCountDownText();

        // 2. Slider Logic
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Prevent 0 minutes
                if (progress < 1) progress = 1;

                durationLabel.setText("Focus Duration: " + progress + " min");
                selectedTimeInMillis = progress * 60 * 1000L;

                // Update the timer text immediately while sliding
                timeLeftInMillis = selectedTimeInMillis;
                updateCountDownText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 3. Button Logic
        startButton.setOnClickListener(v -> startSession());
        emergencyButton.setOnClickListener(v -> stopSession());
        selectAppsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AppSelectionActivity.class);
            startActivity(intent);
        });

        Button historyButton = findViewById(R.id.btn_view_history);
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllowedApps();

        // 1. Check Storage: Should a session be running?
        SharedPreferences prefs = getSharedPreferences("ZenCorePrefs", MODE_PRIVATE);
        boolean isRunning = prefs.getBoolean("IS_SESSION_RUNNING", false);
        long endTime = prefs.getLong("SESSION_END_TIME", 0);
        long now = System.currentTimeMillis();

        if (isRunning && endTime > now) {
            // A session IS active!

            // A. Restore UI State (Hide buttons)
            durationSeekBar.setVisibility(View.INVISIBLE);
            durationLabel.setVisibility(View.INVISIBLE);
            startButton.setVisibility(View.INVISIBLE);
            selectAppsButton.setVisibility(View.INVISIBLE);
            emergencyButton.setVisibility(View.VISIBLE);
            allowedAppsRecycler.setVisibility(View.VISIBLE);

            // B. FORCE RE-LOCK
            try {
                // Check if we are already locked to avoid flickering
                startLockTask();
            } catch (Exception e) { }

            // C. Restart the Timer with correct remaining time
            startTimer(endTime);
        }
    }

    private void loadAllowedApps() {
        SharedPreferences prefs = getSharedPreferences("ZenCorePrefs", MODE_PRIVATE);
        Set<String> allowedPackages = prefs.getStringSet("WhitelistedApps", new HashSet<>());

        List<ApplicationInfo> appList = new ArrayList<>();
        PackageManager pm = getPackageManager();

        for (String packageName : allowedPackages) {
            try {
                appList.add(pm.getApplicationInfo(packageName, 0));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        FocusAppsAdapter adapter = new FocusAppsAdapter(this, appList, this);
        allowedAppsRecycler.setLayoutManager(new GridLayoutManager(this, 4));
        allowedAppsRecycler.setAdapter(adapter);
    }
    // Inside MainActivity.java
    private void startTimer(long endTime) {
        if (countDownTimer != null) countDownTimer.cancel();

        // Calculate how much time is actually left right now
        long timeNow = System.currentTimeMillis();
        long timeRemaining = endTime - timeNow;

        if (timeRemaining <= 0) {
            stopSession(); // It already finished while we were gone
            return;
        }

        timeLeftInMillis = timeRemaining;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownText();
                stopSession();

                // Database save logic here...
                int minutesFocused = (int) (selectedTimeInMillis / 1000) / 60;
                if(minutesFocused < 1) minutesFocused = 1;
                if(dbHelper.addSession(minutesFocused)) {
                    Toast.makeText(MainActivity.this, "Session Saved!", Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    private void startSession() {
        // 1. Calculate when the session should end (e.g., Now + 25 mins)
        long endTime = System.currentTimeMillis() + selectedTimeInMillis;

        // 2. SAVE IT to Storage immediately
        SharedPreferences prefs = getSharedPreferences("ZenCorePrefs", MODE_PRIVATE);
        prefs.edit()
                .putLong("SESSION_END_TIME", endTime)
                .putBoolean("IS_SESSION_RUNNING", true)
                .apply();

        // 3. Update UI (Same as before)
        timeLeftInMillis = selectedTimeInMillis;
        durationSeekBar.setVisibility(View.INVISIBLE);
        durationLabel.setVisibility(View.INVISIBLE);
        selectAppsButton.setVisibility(View.INVISIBLE);
        startButton.setVisibility(View.INVISIBLE);
        emergencyButton.setVisibility(View.VISIBLE);
        allowedAppsRecycler.setVisibility(View.VISIBLE);

        // 4. Lock Logic (Same as before)
        try {
            startLockTask();
        } catch (Exception e) { }

        // 5. Start the Timer
        startTimer(endTime); // We moved the timer logic to a helper function
    }

    private void stopSession() {
        if (countDownTimer != null) countDownTimer.cancel();

        // 1. CLEAR Storage
        SharedPreferences prefs = getSharedPreferences("ZenCorePrefs", MODE_PRIVATE);
        prefs.edit()
                .remove("SESSION_END_TIME")
                .putBoolean("IS_SESSION_RUNNING", false)
                .apply();

        // 2. Unlock
        try {
            stopLockTask();
        } catch (Exception e) { }

        // 3. Reset UI
        timeLeftInMillis = selectedTimeInMillis;
        updateCountDownText();
        durationSeekBar.setVisibility(View.VISIBLE);
        durationLabel.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.VISIBLE);
        selectAppsButton.setVisibility(View.VISIBLE);
        emergencyButton.setVisibility(View.INVISIBLE);
    }


    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }
    private void updateSystemWhitelist() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminName = new ComponentName(this, MainActivity.class);

            // Only works if we are God Mode (Device Owner)
            if (dpm.isDeviceOwnerApp(getPackageName())) {
                SharedPreferences prefs = getSharedPreferences("ZenCorePrefs", MODE_PRIVATE);
                Set<String> savedApps = prefs.getStringSet("WhitelistedApps", new HashSet<>());

                // CRITICAL: The list MUST include:
                // 1. Zen-Core itself (com.ashwath.zen_core)
                // 2. The apps you selected (e.g., com.google.android.calculator)
                Set<String> combinedApps = new HashSet<>(savedApps);
                combinedApps.add(getPackageName());

                // Convert to Array and Send to OS
                String[] packagesArray = combinedApps.toArray(new String[0]);
                dpm.setLockTaskPackages(adminName, packagesArray);
            }
        } catch (Exception e) {
            // Log error
        }
    }

    @Override
    public void onAppClick(String packageName) {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        // CHECK: Are we in God Mode (Device Owner)?
        if (dpm.isDeviceOwnerApp(getPackageName())) {
            // SCENARIO A: God Mode is Active
            // We DO NOT stop the lock. We just launch the app directly.
            // The OS will allow it because we added it to setLockTaskPackages earlier.
            try {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launchIntent);
                } else {
                    Toast.makeText(this, "App not found!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error launching app.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // SCENARIO B: Normal Mode (Soft Lock)
            // We MUST unlock briefly to switch apps, or it crashes.
            try {
                stopLockTask();
            } catch (Exception e) { }

            new android.os.Handler().postDelayed(() -> {
                try {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(launchIntent);
                    }
                } catch (Exception e) { }
            }, 500); // Increased delay to 500ms for safety
        }
    }
}
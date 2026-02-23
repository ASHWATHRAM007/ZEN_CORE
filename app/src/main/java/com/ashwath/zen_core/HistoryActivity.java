package com.ashwath.zen_core;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recycler_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get Data from Database
        dbHelper = new DatabaseHelper(this);
        List<String> sessions = dbHelper.getAllSessions();

        // Show in List
        HistoryAdapter adapter = new HistoryAdapter(sessions);
        recyclerView.setAdapter(adapter);
    }
}
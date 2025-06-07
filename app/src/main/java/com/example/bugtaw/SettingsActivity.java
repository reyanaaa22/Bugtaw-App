package com.example.bugtaw;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.resetAlarmsButton).setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Remove All Alarms")
                .setMessage("Are you sure you want to remove all alarms?")
                .setPositiveButton("OK", (dialog, which) -> {
                    com.example.bugtaw.data.AlarmDbHelper dbHelper = new com.example.bugtaw.data.AlarmDbHelper(this);
                    dbHelper.deleteAllAlarms();
                    android.widget.Toast.makeText(this, "All alarms removed", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
} 
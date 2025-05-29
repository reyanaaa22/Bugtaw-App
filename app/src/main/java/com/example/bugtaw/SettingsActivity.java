package com.example.bugtaw;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "BugtawPrefs";
    private static final String DARK_MODE_KEY = "dark_mode";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        // Initialize preferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Set up dark mode switch
        MaterialSwitch darkModeSwitch = findViewById(R.id.darkModeSwitch);
        darkModeSwitch.setChecked(prefs.getBoolean(DARK_MODE_KEY, true));
        
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            prefs.edit().putBoolean(DARK_MODE_KEY, isChecked).apply();
            
            // Update theme
            AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
} 
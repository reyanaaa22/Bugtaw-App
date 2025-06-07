package com.example.bugtaw;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.Button;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bugtaw.adapter.AlarmAdapter;
import com.example.bugtaw.data.Alarm;
import com.example.bugtaw.data.AlarmDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.ItemTouchHelper;
import android.app.AlertDialog;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.OnAlarmActionListener {
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "BugtawPrefs";
    private static final String DARK_MODE_KEY = "dark_mode";

    private AlarmDbHelper dbHelper;
    private AlarmAdapter alarmAdapter;
    private RecyclerView alarmRecyclerView;
    private TextView emptyView;
    private TextView currentTimeText;
    private TextView currentDateText;
    private AlarmManager alarmManager;
    private Handler timeUpdateHandler;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set up preferences and theme
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(DARK_MODE_KEY, true); // Default to dark mode
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        darkModeSwitch = navView.findViewById(R.id.darkModeSwitch);
        resetAlarmsButton = navView.findViewById(R.id.resetAlarmsButton);

        // Set dark mode switch state
        darkModeSwitch.setChecked(prefs.getBoolean(DARK_MODE_KEY, true));
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(DARK_MODE_KEY, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate(); // Force activity to recreate for instant theme update
        });

        // Reset alarms button
        resetAlarmsButton.setOnClickListener(v -> {
            dbHelper.deleteAllAlarms();
            loadAlarms();
            Toast.makeText(this, "All alarms removed", Toast.LENGTH_SHORT).show();
        });

        // Initialize views and formatters
        currentTimeText = findViewById(R.id.currentTimeText);
        currentDateText = findViewById(R.id.currentDateText);
        timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());

        // Initialize database helper and alarm manager
        dbHelper = new AlarmDbHelper(this);

        // Attach swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Alarm alarmToDelete = alarmAdapter.getAlarmAt(position);
                // Show confirmation dialog
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete Alarm")
                    .setMessage("Are you sure you want to delete this alarm?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        dbHelper.deleteAlarm(alarmToDelete.getId());
                        loadAlarms();
                        Toast.makeText(MainActivity.this, "Alarm deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        alarmAdapter.notifyItemChanged(position); // Restore item if cancelled
                    })
                    .setCancelable(false)
                    .show();
            }
        });
        itemTouchHelper.attachToRecyclerView(alarmRecyclerView);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Initialize views
        alarmRecyclerView = findViewById(R.id.alarmRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        FloatingActionButton addAlarmFab = findViewById(R.id.addAlarmFab);

        // Setup RecyclerView
        alarmAdapter = new AlarmAdapter(this);
        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alarmRecyclerView.setAdapter(alarmAdapter);

        // Set FAB click listener
        addAlarmFab.setOnClickListener(v -> {
            // Check for exact alarm permission on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                    Toast.makeText(this, "Please enable exact alarms permission", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Intent intent = new Intent(MainActivity.this, AlarmSetupActivity.class);
            startActivity(intent);
        });

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        }

        // Start time updates
        timeUpdateHandler = new Handler(Looper.getMainLooper());
        startTimeUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlarms();
        // Reschedule all enabled alarms
        List<Alarm> alarms = dbHelper.getAllAlarms();
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                scheduleAlarm(alarm);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeUpdateHandler.removeCallbacksAndMessages(null);
    }

    private void startTimeUpdates() {
        updateTime(); // Initial update
        timeUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTime();
                timeUpdateHandler.postDelayed(this, 1000); // Update every second
            }
        }, 1000);
    }

    private void updateTime() {
        Calendar calendar = Calendar.getInstance();
        currentTimeText.setText(timeFormat.format(calendar.getTime()));
        currentDateText.setText(dateFormat.format(calendar.getTime()));
    }

    private void loadAlarms() {
        List<Alarm> alarms = dbHelper.getAllAlarms();
        alarmAdapter.setAlarms(alarms);
        
        // Show/hide empty view
        if (alarms.isEmpty()) {
            alarmRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            alarmRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAlarmToggled(Alarm alarm, boolean isEnabled) {
        dbHelper.toggleAlarmEnabled(alarm.getId(), isEnabled);
        if (isEnabled) {
            scheduleAlarm(alarm);
        } else {
            cancelAlarm(alarm);
        }
    }

    @Override
    public void onAlarmClicked(Alarm alarm) {
        Intent intent = new Intent(this, AlarmSetupActivity.class);
        intent.putExtra("alarm_id", alarm.getId());
        intent.putExtra("hour", alarm.getHour());
        intent.putExtra("minute", alarm.getMinute());
        intent.putExtra("days", alarm.getDays());
        intent.putExtra("puzzle_type", alarm.getPuzzleType());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private SwitchCompat darkModeSwitch;
    private Button resetAlarmsButton;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scheduleAlarm(Alarm alarm) {
        Log.d(TAG, "Scheduling alarm " + alarm.getId());
        
        // Create intent for AlarmReceiver
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("alarm_id", alarm.getId());
        intent.putExtra("puzzle_type", alarm.getPuzzleType());

        // Create unique pending intent for this alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            (int) alarm.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Get current time
        Calendar now = Calendar.getInstance();
        
        // Calculate next alarm time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If alarm time has passed for today, move to next occurrence
        if (calendar.getTimeInMillis() <= now.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Find the next day that is selected
        String[] selectedDays = alarm.getDays().split(",");
        boolean foundNextDay = false;
        int daysChecked = 0;

        while (!foundNextDay && daysChecked < 7) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // Convert Calendar.DAY_OF_WEEK to our day numbering (1=Monday, 7=Sunday)
            int ourDayOfWeek = dayOfWeek == Calendar.SUNDAY ? 7 : dayOfWeek - 1;
            
            for (String day : selectedDays) {
                if (Integer.parseInt(day) == ourDayOfWeek) {
                    foundNextDay = true;
                    break;
                }
            }
            
            if (!foundNextDay) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            daysChecked++;
        }

        if (foundNextDay) {
            // Schedule the alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
                );
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
                );
            }
            Log.d(TAG, "Alarm scheduled for: " + calendar.getTime().toString());
        }
    }

    private void cancelAlarm(Alarm alarm) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            (int) alarm.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}

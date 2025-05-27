package com.example.bugtaw;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bugtaw.data.AlarmDbHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AlarmSetupActivity extends AppCompatActivity {
    private TimePicker timePicker;
    private ChipGroup daysChipGroup;
    private RadioGroup puzzleTypeGroup;
    private AlarmDbHelper dbHelper;
    private long alarmId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setup);

        // Initialize views
        timePicker = findViewById(R.id.timePicker);
        daysChipGroup = findViewById(R.id.daysChipGroup);
        puzzleTypeGroup = findViewById(R.id.puzzleTypeGroup);
        Button saveButton = findViewById(R.id.saveButton);

        // Initialize database helper
        dbHelper = new AlarmDbHelper(this);

        // Check if we're editing an existing alarm
        if (getIntent().hasExtra("alarm_id")) {
            alarmId = getIntent().getLongExtra("alarm_id", -1);
            int hour = getIntent().getIntExtra("hour", 0);
            int minute = getIntent().getIntExtra("minute", 0);
            String days = getIntent().getStringExtra("days");
            String puzzleType = getIntent().getStringExtra("puzzle_type");

            // Set the time
            timePicker.setHour(hour);
            timePicker.setMinute(minute);

            // Set the days
            setSelectedDays(days);

            // Set the puzzle type
            setPuzzleType(puzzleType);
        }

        // Set save button click listener
        saveButton.setOnClickListener(v -> saveAlarm());
    }

    private void setSelectedDays(String days) {
        if (days != null) {
            String[] selectedDays = days.split(",");
            for (String day : selectedDays) {
                int dayNumber = Integer.parseInt(day);
                int chipId;
                switch (dayNumber) {
                    case 1: chipId = R.id.mondayChip; break;
                    case 2: chipId = R.id.tuesdayChip; break;
                    case 3: chipId = R.id.wednesdayChip; break;
                    case 4: chipId = R.id.thursdayChip; break;
                    case 5: chipId = R.id.fridayChip; break;
                    case 6: chipId = R.id.saturdayChip; break;
                    case 7: chipId = R.id.sundayChip; break;
                    default: continue;
                }
                Chip chip = findViewById(chipId);
                if (chip != null) {
                    chip.setChecked(true);
                }
            }
        }
    }

    private void setPuzzleType(String puzzleType) {
        if (puzzleType != null) {
            int radioId;
            switch (puzzleType) {
                case "Math Problem":
                    radioId = R.id.mathPuzzle;
                    break;
                case "Memory Recall":
                    radioId = R.id.memoryPuzzle;
                    break;
                case "Pattern Tap":
                    radioId = R.id.patternPuzzle;
                    break;
                default:
                    return;
            }
            RadioButton radioButton = findViewById(radioId);
            if (radioButton != null) {
                radioButton.setChecked(true);
            }
        }
    }

    private void saveAlarm() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String days = getSelectedDays();
        String puzzleType = getSelectedPuzzleType();

        if (days.isEmpty()) {
            Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show();
            return;
        }

        if (alarmId == -1) {
            // Insert new alarm
            dbHelper.insertAlarm(hour, minute, days, puzzleType);
        } else {
            // Update existing alarm
            dbHelper.updateAlarm(alarmId, hour, minute, days, puzzleType, true);
        }

        finish();
    }

    private String getSelectedDays() {
        List<String> selectedDays = new ArrayList<>();
        for (int i = 0; i < daysChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) daysChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                int id = chip.getId();
                if (id == R.id.mondayChip) selectedDays.add("1");
                else if (id == R.id.tuesdayChip) selectedDays.add("2");
                else if (id == R.id.wednesdayChip) selectedDays.add("3");
                else if (id == R.id.thursdayChip) selectedDays.add("4");
                else if (id == R.id.fridayChip) selectedDays.add("5");
                else if (id == R.id.saturdayChip) selectedDays.add("6");
                else if (id == R.id.sundayChip) selectedDays.add("7");
            }
        }
        return String.join(",", selectedDays);
    }

    private String getSelectedPuzzleType() {
        int selectedId = puzzleTypeGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.mathPuzzle) {
            return "Math Problem";
        } else if (selectedId == R.id.memoryPuzzle) {
            return "Memory Recall";
        } else if (selectedId == R.id.patternPuzzle) {
            return "Pattern Tap";
        }
        return "Math Problem"; // Default puzzle type
    }
} 
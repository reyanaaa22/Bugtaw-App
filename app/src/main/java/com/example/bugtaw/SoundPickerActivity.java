package com.example.bugtaw;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SoundPickerActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_AUDIO = 1001;
    private Switch vibrateSwitch;
    private Button btnPickFile, btnConfirm;
    private RecyclerView ringtoneList;
    private MediaPlayer previewPlayer;
    private String selectedSound;
    private boolean vibrate = true;
    private RingtoneAdapter adapter;
    private List<String> ringtoneNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_picker);

        vibrateSwitch = findViewById(R.id.vibrate_switch);
        btnPickFile = findViewById(R.id.btn_pick_file);
        btnConfirm = findViewById(R.id.btn_confirm_sound);
        ringtoneList = findViewById(R.id.ringtone_list);

        vibrateSwitch.setChecked(true);
        vibrateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> vibrate = isChecked);

        btnPickFile.setOnClickListener(v -> openAudioFilePicker());
        btnConfirm.setOnClickListener(v -> confirmSelection());
        btnConfirm.setEnabled(false);

        loadClassicRingtones();
        setupRecyclerView();
    }

    private void loadClassicRingtones() {
        // Use reflection to get all resource names in R.raw
        try {
            Field[] fields = R.raw.class.getFields();
            for (Field field : fields) {
                ringtoneNames.add(field.getName());
            }
        } catch (Exception e) {
            ringtoneNames.add("alarm_sound");
        }
    }

    private void setupRecyclerView() {
        adapter = new RingtoneAdapter(ringtoneNames, this::onRingtoneSelected, this::onRingtonePreview);
        ringtoneList.setLayoutManager(new LinearLayoutManager(this));
        ringtoneList.setAdapter(adapter);
    }

    private void onRingtoneSelected(String name) {
        selectedSound = name;
        btnConfirm.setEnabled(true);
        onRingtonePreview(name);
    }

    private void onRingtonePreview(String name) {
        stopPreview();
        int resId = getResources().getIdentifier(name, "raw", getPackageName());
        if (resId != 0) {
            previewPlayer = MediaPlayer.create(this, resId);
            previewPlayer.setOnCompletionListener(mp -> stopPreview());
            previewPlayer.start();
        } else {
            Toast.makeText(this, "Cannot play sound", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPreview() {
        if (previewPlayer != null) {
            previewPlayer.stop();
            previewPlayer.release();
            previewPlayer = null;
        }
    }

    private void openAudioFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_PICK_AUDIO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_AUDIO && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                selectedSound = uri.toString();
                btnConfirm.setEnabled(true);
                stopPreview();
            }
        }
    }

    private void confirmSelection() {
        Intent result = new Intent();
        result.putExtra("sound", selectedSound);
        result.putExtra("vibrate", vibrate);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPreview();
    }
}

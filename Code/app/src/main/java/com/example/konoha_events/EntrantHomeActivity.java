package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konoha_events.auth.EntrantDeviceIdStore;

public class EntrantHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_home);

        // Show the device ID
        String id = EntrantDeviceIdStore.getOrCreateId(this);
        TextView tv = findViewById(R.id.text_device_id);
        tv.setText("Entrant Home\n\nDevice ID:\n" + (id == null ? "(none yet)" : id));

        // Handle viewGuidelines button
        Button btnGuidelines = findViewById(R.id.button_view_guidelines);
        btnGuidelines.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantHomeActivity.this, GuidelinesActivity.class);
            startActivity(intent);
        });
    }
}
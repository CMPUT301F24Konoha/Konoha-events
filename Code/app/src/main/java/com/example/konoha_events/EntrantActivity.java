package com.example.konoha_events;   // adjust to your actual package

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.example.konoha_events.ProfileActivity;

public class EntrantActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_all_events);
        ImageButton profileButton = findViewById(R.id.profile);

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}

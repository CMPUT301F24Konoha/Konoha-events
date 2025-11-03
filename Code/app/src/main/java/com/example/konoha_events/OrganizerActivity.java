package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import services.FirebaseService;

/**
 * OrganizerActivity
 * ----------------------
 * Displays the Organizer Dashboard UI.
 * - Provides navigation to create events.
 */

public class OrganizerActivity extends AppCompatActivity {
    private Button createEventButton;
    private Button manageEventsButton;
    private LinearLayout eventsContainer;
    private FirebaseService fbs;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        fbs = FirebaseService.firebaseService;

        // Get deviceId from intent passed from LoginActivity
        deviceId = getIntent().getStringExtra("deviceId");

        if (deviceId == null) {
            deviceId = "12345"; // temporary fix
            Toast.makeText(this, "Using temporary organizer ID", Toast.LENGTH_SHORT).show();
        }

        initializeViews();
        setupCreateEventButton();
        setupManageEventButton();
    }

    public void initializeViews() {
        // Initialize buttons
        createEventButton = findViewById(R.id.createEventButton);
        manageEventsButton = findViewById(R.id.activity_organizer_create_event_button);

        // Get parent view of createEventButton
        LinearLayout parentLayout = (LinearLayout) createEventButton.getParent();

        // Create container for events
        eventsContainer = new LinearLayout(this);
        eventsContainer.setOrientation(LinearLayout.VERTICAL);
        eventsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        parentLayout.addView(eventsContainer);
    }

    public void setupCreateEventButton() {
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });
    }

    public void setupManageEventButton() {
        manageEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrganizerViewEventListActivity.class);
            startActivity(intent);
        });
    }
}

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
 * - Allows organizer to create new events.
 * - Allows organizer to view entrant locations on a map.
 */
public class OrganizerActivity extends AppCompatActivity {
    private Button createEventButton;
    private Button viewEntrantLocationsButton;
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
            deviceId = "12345"; // temporary fallback ID
            Toast.makeText(this, "Using temporary organizer ID", Toast.LENGTH_SHORT).show();
        }

        initializeViews();
        setupCreateEventButton();
        setupViewEntrantLocationsButton();
    }

    /**
     * Initialize buttons and layout containers.
     */
    public void initializeViews() {
        // Initialize buttons
        createEventButton = findViewById(R.id.createEventButton);
        viewEntrantLocationsButton = findViewById(R.id.btnViewEntrantLocations);

        // Get parent layout of the createEventButton
        LinearLayout parentLayout = (LinearLayout) createEventButton.getParent();

        // Create a container for dynamically loaded events
        eventsContainer = new LinearLayout(this);
        eventsContainer.setOrientation(LinearLayout.VERTICAL);
        eventsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        parentLayout.addView(eventsContainer);
    }

    /**
     * Set up the button for creating new events.
     */
    public void setupCreateEventButton() {
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        });
    }

    /**
     * Set up the button for viewing entrant locations on a map.
     */
    public void setupViewEntrantLocationsButton() {
        if (viewEntrantLocationsButton != null) {
            viewEntrantLocationsButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrganizerMapActivity.class);
                startActivity(intent);
            });
        } else {
            Toast.makeText(this, "Map button not found in layout", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.konoha_events.auth.EntrantDeviceIdStore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import constants.IntentConstants;
import models.EventModel;
import services.FirebaseService;

/**
 * OrganizerActivity
 * ----------------------
 * Displays the Organizer Dashboard UI.
 * - Allows organizer to create new events.
 * - Allows organizer to view entrant locations on a map.
 * - Provides navigation to create events.
 * - Shows list of organizer's events with QR code viewing
 */
public class OrganizerActivity extends AppCompatActivity {
    private Button createEventButton;
    private Button viewEntrantLocationsButton;
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

        initializeViews();
        setupCreateEventButton();
        setupManageEventButton();
        loadOrganizerEvents();
        setupViewEntrantLocationsButton();
    }

    /**
     * Initialize buttons and layout containers.
     */
    public void initializeViews() {
        // Initialize buttons
        createEventButton = findViewById(R.id.createEventButton);
        viewEntrantLocationsButton = findViewById(R.id.btnViewEntrantLocations);
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
        eventsContainer.setPadding(16, 16, 16, 16);

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
     * Load and display events created by this organizer
     */
    private void loadOrganizerEvents() {
        fbs.getEventsLiveData().observe(this, new Observer<ArrayList<EventModel>>() {
            @Override
            public void onChanged(ArrayList<EventModel> events) {
                displayEvents(events);
            }
        });
    }

    /**
     * Display the organizer's events in the container
     */
    private void displayEvents(ArrayList<EventModel> allEvents) {
        eventsContainer.removeAllViews();

        String deviceId = EntrantDeviceIdStore.getOrCreateId(this);

        if (deviceId == null) {
            deviceId = "12345";
        }

        // Filter events by this organizer's device ID
        ArrayList<EventModel> organizerEvents = new ArrayList<>();
        for (EventModel event : allEvents) {
            if (deviceId.equals(event.getDeviceId())) {
                organizerEvents.add(event);
            }
        }

        if (organizerEvents.isEmpty()) {
            TextView noEventsText = new TextView(this);
            noEventsText.setText("No events created yet. Tap 'Create Event' to get started!");
            noEventsText.setGravity(Gravity.CENTER);
            noEventsText.setPadding(16, 32, 16, 32);
            eventsContainer.addView(noEventsText);
            return;
        }

        // Display each event
        for (EventModel event : organizerEvents) {
            LinearLayout eventCard = createEventCard(event);
            eventsContainer.addView(eventCard);
        }
    }

    /**
     * Create a card view for an event
     */
    private LinearLayout createEventCard(EventModel event) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(24, 24, 24, 24);
        card.setBackgroundColor(0xFFF5F5F5); // Light gray background

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);

        // Event Title
        TextView titleText = new TextView(this);
        titleText.setText(event.getEventTitle() != null ? event.getEventTitle() : "Untitled Event");
        titleText.setTextSize(20f);
        titleText.setTextColor(0xFF000000);
        titleText.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_START);
        card.addView(titleText);

        // Event Description (truncated)
        if (event.getDescription() != null) {
            TextView descText = new TextView(this);
            String description = event.getDescription();
            if (description.length() > 100) {
                description = description.substring(0, 100) + "...";
            }
            descText.setText(description);
            descText.setTextSize(14f);
            descText.setTextColor(0xFF666666);
            descText.setPadding(0, 8, 0, 8);
            card.addView(descText);
        }

        // Registration Deadline
        if (event.getRegistrationDeadline() != null) {
            TextView deadlineText = new TextView(this);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            deadlineText.setText("Deadline: " + dateFormat.format(event.getRegistrationDeadline()));
            deadlineText.setTextSize(12f);
            deadlineText.setTextColor(0xFF666666);
            deadlineText.setPadding(0, 4, 0, 8);
            card.addView(deadlineText);
        }

        // Buttons container
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setPadding(0, 16, 0, 0);

        // View QR Code Button
        Button viewQRButton = new Button(this);
        viewQRButton.setText("View QR Code");
        viewQRButton.setTextSize(14f);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        buttonParams.setMargins(0, 0, 8, 0);
        viewQRButton.setLayoutParams(buttonParams);
        viewQRButton.setOnClickListener(v -> openQRCodeView(event.getId()));
        buttonsLayout.addView(viewQRButton);

        // View Details Button
        Button viewDetailsButton = new Button(this);
        viewDetailsButton.setText("View Details");
        viewDetailsButton.setTextSize(14f);
        LinearLayout.LayoutParams detailsButtonParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        detailsButtonParams.setMargins(8, 0, 0, 0);
        viewDetailsButton.setLayoutParams(detailsButtonParams);
        viewDetailsButton.setOnClickListener(v -> openEventDetails(event.getId()));
        buttonsLayout.addView(viewDetailsButton);

        card.addView(buttonsLayout);

        return card;
    }

    /**
     * Open the QR code view for an event
     */
    private void openQRCodeView(String eventId) {
        Intent intent = new Intent(this, EventQRCodeActivity.class);
        intent.putExtra(IntentConstants.INTENT_EVENT_ID, eventId);
        startActivity(intent);
    }

    /**
     * Open the event details view
     */
    private void openEventDetails(String eventId) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra(IntentConstants.INTENT_EVENT_ID, eventId);
        startActivity(intent);
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

    public void setupManageEventButton() {
        manageEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrganizerViewEventListActivity.class);
            startActivity(intent);
        });
    }
}

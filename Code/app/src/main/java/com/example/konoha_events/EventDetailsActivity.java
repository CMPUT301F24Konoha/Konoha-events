package com.example.konoha_events;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import constants.IntentConstants;
import models.EventModel;
import services.FirebaseService;
import util.ModelUtil;

/**
 * EventDetailsActivity
 * ----------------------
 * Displays detailed information about an event after scanning a QR code
 * Allows entrants to join the waitlist for the event
 */
public class EventDetailsActivity extends AppCompatActivity {

    private TextView eventTitle, eventDescription, eventDeadline, entrantLimit;
    private ImageView eventPoster;
    private Button joinWaitlistButton, backButton;
    private ProgressBar loadingSpinner;

    private FirebaseService fbs;
    private String eventId;
    private EventModel currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        fbs = FirebaseService.firebaseService;

        // Get event ID from intent
        eventId = getIntent().getStringExtra(IntentConstants.INTENT_EVENT_ID);

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadEventDetails();
    }

    /**
     * Initialize all view components
     */
    private void initializeViews() {
        eventTitle = findViewById(R.id.eventTitle);
        eventDescription = findViewById(R.id.eventDescription);
        eventDeadline = findViewById(R.id.eventDeadline);
        entrantLimit = findViewById(R.id.entrantLimit);
        eventPoster = findViewById(R.id.eventPoster);
        // TODO: hide join waitlist button if the user is an organizer
        joinWaitlistButton = findViewById(R.id.joinWaitlistButton);
        backButton = findViewById(R.id.backButton);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        // Set up button listeners
        backButton.setOnClickListener(v -> finish());
        joinWaitlistButton.setOnClickListener(v -> joinWaitlist());
    }

    /**
     * Load event details from Firebase
     */
    private void loadEventDetails() {
        showLoading(true);

        fbs.getEventDocumentReference(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentEvent = ModelUtil.toEventModel(documentSnapshot);
                        displayEventDetails();
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    finish();
                });
    }

    /**
     * Display the loaded event details in the UI
     */
    private void displayEventDetails() {
        if (currentEvent == null) return;

        // Set event title
        if (currentEvent.getEventTitle() != null && !currentEvent.getEventTitle().isEmpty()) {
            eventTitle.setText(currentEvent.getEventTitle());
        } else {
            eventTitle.setText("Untitled Event");
        }

        // Set event description
        if (currentEvent.getDescription() != null && !currentEvent.getDescription().isEmpty()) {
            eventDescription.setText(currentEvent.getDescription());
        } else {
            eventDescription.setText("No description available");
        }

        // Set registration deadline
        if (currentEvent.getRegistrationDeadline() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String deadlineText = "Registration Deadline: " +
                    dateFormat.format(currentEvent.getRegistrationDeadline());
            eventDeadline.setText(deadlineText);

            // Check if registration is still open
            if (!currentEvent.isRegistrationOpen()) {
                eventDeadline.append(" (CLOSED)");
                joinWaitlistButton.setEnabled(false);
                joinWaitlistButton.setText("Registration Closed");
                joinWaitlistButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            eventDeadline.setText("Registration Deadline: No deadline set");
        }

        // Set entrant limit
        if (currentEvent.hasEntrantLimit()) {
            String limitText = "Entrant Limit: " + currentEvent.getEntrantLimit() + " participants";
            entrantLimit.setText(limitText);
            entrantLimit.setVisibility(View.VISIBLE);
        } else {
            entrantLimit.setText("Entrant Limit: Unlimited");
            entrantLimit.setVisibility(View.VISIBLE);
        }

        // Load event poster if available
        if (currentEvent.getImageBitmap() != null) {
            loadEventImage(currentEvent.getImageBitmap());
        } else {
            eventPoster.setVisibility(View.GONE);
        }
    }

    private void loadEventImage(Bitmap imageBitmap) {
        eventPoster.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imageBitmap)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background) // Shows while loading
                .error(R.drawable.ic_launcher_background) // Shows if load fails
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image
                .into(eventPoster);
    }

    /**
     * Handle joining the waitlist
     */
    private void joinWaitlist() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if registration is still open
        if (!currentEvent.isRegistrationOpen()) {
            Toast.makeText(this, "Registration deadline has passed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user ID
        String userId = fbs.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Please log in to join the waitlist", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to login screen
            return;
        }

        // TODO: Implement actual waitlist joining logic
        // TODO: Check if user is already on waitlist
        // TODO: Check if waitlist is full (if entrant limit exists)

        Toast.makeText(this, "Waitlist functionality coming soon", Toast.LENGTH_SHORT).show();
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean show) {
        if (show) {
            loadingSpinner.setVisibility(View.VISIBLE);
            eventTitle.setVisibility(View.GONE);
            eventDescription.setVisibility(View.GONE);
            eventDeadline.setVisibility(View.GONE);
            entrantLimit.setVisibility(View.GONE);
            eventPoster.setVisibility(View.GONE);
            joinWaitlistButton.setVisibility(View.GONE);
        } else {
            loadingSpinner.setVisibility(View.GONE);
            eventTitle.setVisibility(View.VISIBLE);
            eventDescription.setVisibility(View.VISIBLE);
            eventDeadline.setVisibility(View.VISIBLE);
            // entrantLimit and eventPoster visibility set in displayEventDetails()
            joinWaitlistButton.setVisibility(View.VISIBLE);
        }
    }
}
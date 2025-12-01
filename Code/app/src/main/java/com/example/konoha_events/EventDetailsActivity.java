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

import constants.DatabaseConstants;
import constants.IntentConstants;
import models.EventModel;
import models.OnWaitingListModel;
import services.FirebaseService;
import util.ModelUtil;

/**
 * EventDetailsActivity
 * ----------------------
 * Displays detailed information about an event
 * Allows entrants to join the waitlist for the event
 * Display waitlist count
 */
public class EventDetailsActivity extends AppCompatActivity {

    private TextView eventTitle, eventDescription, eventDeadline, entrantLimit, waitlistCount;
    private ImageView eventPoster;
    private Button joinWaitlistButton, backButton;
    private ProgressBar loadingSpinner;

    private FirebaseService fbs;
    private String eventId;
    private EventModel currentEvent;
    private int currentWaitlistCount = 0;

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
        observeWaitlistChanges();
    }

    /**
     * Initialize all view components
     */
    private void initializeViews() {
        eventTitle = findViewById(R.id.eventTitle);
        eventDescription = findViewById(R.id.eventDescription);
        eventDeadline = findViewById(R.id.eventDeadline);
        entrantLimit = findViewById(R.id.entrantLimit);
        waitlistCount = findViewById(R.id.waitlistCount);
        eventPoster = findViewById(R.id.eventPoster);
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
                        updateWaitlistButton();
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
     * Observe changes to the waitlist for this event
     * Display waitlist count
     */
    private void observeWaitlistChanges() {
        fbs.getOnWaitingListLiveData().observe(this, waitlistModels -> {
            if (waitlistModels == null) return;

            // Count users on the waitlist for this event
            int count = 0;
            for (OnWaitingListModel model : waitlistModels) {
                if (model != null && eventId.equals(model.getEventId())) {
                    count++;
                }
            }

            currentWaitlistCount = count;
            updateWaitlistCountDisplay();
            updateWaitlistButton();
        });
    }

    /**
     * Update the waitlist count display
     */
    private void updateWaitlistCountDisplay() {
        if (waitlistCount != null) {
            String countText = "Waitlist: " + currentWaitlistCount + " entrant" +
                    (currentWaitlistCount != 1 ? "s" : "");
            waitlistCount.setText(countText);
            waitlistCount.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Update the join waitlist button based on user's current status
     */
    private void updateWaitlistButton() {
        String userId = fbs.getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            joinWaitlistButton.setText("Login to Join Waitlist");
            joinWaitlistButton.setEnabled(false);
            return;
        }

        // Check if user is already on the waitlist
        OnWaitingListModel existing = fbs.getExistingWaitlistEntry(eventId, userId);

        if (existing != null) {
            DatabaseConstants.ON_WAITING_LIST_STATUS status = existing.getStatus();

            switch (status) {
                case WAITING:
                    joinWaitlistButton.setText("On Waitlist");
                    joinWaitlistButton.setEnabled(false);
                    joinWaitlistButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                    break;
                case SELECTED:
                    joinWaitlistButton.setText("Selected - Check Invitations");
                    joinWaitlistButton.setEnabled(false);
                    joinWaitlistButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                    break;
                case ACCEPTED:
                    joinWaitlistButton.setText("Registered");
                    joinWaitlistButton.setEnabled(false);
                    joinWaitlistButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case DECLINED:
                    joinWaitlistButton.setText("Previously Declined");
                    joinWaitlistButton.setEnabled(false);
                    joinWaitlistButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    break;
                case CANCELLED:
                    joinWaitlistButton.setText("Cancelled");
                    joinWaitlistButton.setEnabled(false);
                    joinWaitlistButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    break;
                default:
                    joinWaitlistButton.setText("Join Waitlist");
                    joinWaitlistButton.setEnabled(true);
            }
        } else {
            joinWaitlistButton.setText("Join Waitlist");
            joinWaitlistButton.setEnabled(true);
        }

        // Disable if registration is closed
        if (currentEvent != null && !currentEvent.isRegistrationOpen()) {
            joinWaitlistButton.setText("Registration Closed");
            joinWaitlistButton.setEnabled(false);
            joinWaitlistButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }

        // Check if waitlist is full (if entrant limit exists)
        if (currentEvent != null && currentEvent.hasEntrantLimit()) {
            if (currentWaitlistCount >= currentEvent.getEntrantLimit()) {
                OnWaitingListModel existing2 = fbs.getExistingWaitlistEntry(eventId, userId);
                if (existing2 == null) { // Only show full if user is not already on it
                    joinWaitlistButton.setText("Waitlist Full");
                    joinWaitlistButton.setEnabled(false);
                    joinWaitlistButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                }
            }
        }
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

    /**
     * Load event image using Glide
     */
    private void loadEventImage(Bitmap imageBitmap) {
        eventPoster.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imageBitmap)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(eventPoster);
    }

    /**
     * Handle joining the waitlist
     * Sign up for event from event details
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
            return;
        }

        // Check if user is already on waitlist
        OnWaitingListModel existing = fbs.getExistingWaitlistEntry(eventId, userId);
        if (existing != null) {
            Toast.makeText(this, "You are already on the waitlist for this event",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if waitlist is full (if entrant limit exists)
        if (currentEvent.hasEntrantLimit() && currentWaitlistCount >= currentEvent.getEntrantLimit()) {
            Toast.makeText(this, "Waitlist is full", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Join Waitlist")
                .setMessage("Would you like to join the waitlist for \"" +
                        currentEvent.getEventTitle() + "\"?")
                .setPositiveButton("Join", (dialog, which) -> {
                    // Join the waitlist
                    fbs.joinWaitingList(eventId, userId);
                    Toast.makeText(this, "Successfully joined waitlist!",
                            Toast.LENGTH_SHORT).show();

                })
                .setNegativeButton("Cancel", null)
                .show();
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
            if (waitlistCount != null) {
                waitlistCount.setVisibility(View.GONE);
            }
            eventPoster.setVisibility(View.GONE);
            joinWaitlistButton.setVisibility(View.GONE);
        } else {
            loadingSpinner.setVisibility(View.GONE);
            eventTitle.setVisibility(View.VISIBLE);
            eventDescription.setVisibility(View.VISIBLE);
            eventDeadline.setVisibility(View.VISIBLE);
            if (waitlistCount != null) {
                waitlistCount.setVisibility(View.VISIBLE);
            }
            joinWaitlistButton.setVisibility(View.VISIBLE);
        }
    }
}
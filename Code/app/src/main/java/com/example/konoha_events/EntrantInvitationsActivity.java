package com.example.konoha_events;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import constants.DatabaseConstants;
import services.FirebaseService;

public class EntrantInvitationsActivity extends AppCompatActivity {

    private LinearLayout container;
    private FirebaseFirestore db;
    private String entrantId;   // Firestore userId for the logged-in entrant

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_invitations);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Invitations");
        }

        container = findViewById(R.id.container_invitations);
        db = FirebaseFirestore.getInstance();

        // Use the Firestore userId (same one used in joinWaitingList)
        entrantId = FirebaseService.firebaseService.getCurrentUserId();

        if (entrantId == null || entrantId.isEmpty()) {
            showMessage("No logged-in user. Please log in again.");
            return;
        }

        // guideline wiring
        Button guidelinesButton = findViewById(R.id.guidelines_button);
        if (guidelinesButton != null) {
            guidelinesButton.setOnClickListener(v ->
                    startActivity(new android.content.Intent(
                            EntrantInvitationsActivity.this,
                            GuidelinesActivity.class
                    ))
            );
        }

        Button notificationsButton = findViewById(R.id.notifications_button);
        notificationsButton.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(
                    EntrantInvitationsActivity.this,
                    EntrantNotificationsActivity.class
            );
            startActivity(intent);
        });


        loadPendingInvitations();
    }

    /**
     * Loads all invitations for the current entrant with status = SELECTED.
     */
    private void loadPendingInvitations() {
        container.removeAllViews();

        db.collection(DatabaseConstants.COLLECTION_ON_WAITING_LIST_NAME)
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_USER_ID_FIELD, entrantId)
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD,
                        DatabaseConstants.ON_WAITING_LIST_STATUS.SELECTED.name())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        showMessage("You have no pending invitations.");
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        addInvitationCard(
                                doc.getId(),
                                doc.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD)
                        );
                    }
                })
                .addOnFailureListener(e ->
                        showMessage("Failed to load invitations: " + e.getMessage()));
    }

    /**
     * Adds a card to the screen for a single invitation.
     */
    private void addInvitationCard(String docId, String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            eventId = "(unknown event)";
        }

        // Create the card layout
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        card.setPadding(pad, pad, pad, pad);
        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        // Title TextView (we'll fill the actual text after we fetch the event)
        TextView title = new TextView(this);
        title.setTextSize(16f);

        // Optional: show ID as a smaller line under the title
        TextView subtitle = new TextView(this);
        subtitle.setTextSize(12f);
        subtitle.setText("Event ID: " + eventId);

        Button acceptBtn = new Button(this);
        acceptBtn.setText("Accept");
        acceptBtn.setOnClickListener(v ->
                updateInvitationStatus(docId,
                        DatabaseConstants.ON_WAITING_LIST_STATUS.ACCEPTED,
                        card));

        Button declineBtn = new Button(this);
        declineBtn.setText("Decline");
        declineBtn.setOnClickListener(v ->
                updateInvitationStatus(docId,
                        DatabaseConstants.ON_WAITING_LIST_STATUS.DECLINED,
                        card));

        // Add views to card
        card.addView(title);
        card.addView(subtitle);
        card.addView(acceptBtn);
        card.addView(declineBtn);

        // Add the card to the container immediately (UI feels snappy)
        container.addView(card);

        // 🔍 Now fetch the event title from Firestore and update the title TextView
        db.collection(DatabaseConstants.COLLECTION_EVENTS_NAME)
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (eventDoc != null && eventDoc.exists()) {
                        String eventTitle = eventDoc.getString(
                                DatabaseConstants.COLLECTION_EVENTS_TITLE_FIELD);

                        if (eventTitle == null || eventTitle.isEmpty()) {
                            eventTitle = "Event";
                        }

                        title.setText("Event: " + eventTitle);
                    } else {
                        // Fallback if event doesn't exist
                        title.setText("Event: (unknown)");
                    }
                })
                .addOnFailureListener(e -> {
                    // On failure, show a generic label instead of crashing
                    title.setText("Event: (failed to load)");
                });
    }


    /**
     * Updates the invitation status in Firestore for accepted or declined.
     */
    private void updateInvitationStatus(String docId,
                                        DatabaseConstants.ON_WAITING_LIST_STATUS newStatus,
                                        View cardView) {

        db.collection(DatabaseConstants.COLLECTION_ON_WAITING_LIST_NAME)
                .document(docId)
                .update(DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD,
                        newStatus.name())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Invitation " + newStatus.name().toLowerCase(),
                            Toast.LENGTH_SHORT).show();
                    container.removeView(cardView);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to update: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    /**
     * Shows a simple message and, if empty, displays it in the container.
     */
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        if (container.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText(msg);
            container.addView(empty);
        }
    }
}
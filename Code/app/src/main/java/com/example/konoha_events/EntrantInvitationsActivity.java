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

/**
 * activity that shows all pending invitations for the logged-in entrant.
 * each invitation lets the user accept or decline their selection for an event.
 * also provides shortcuts to the guidelines screen and the notifications screen.
 */
public class EntrantInvitationsActivity extends AppCompatActivity {

    /** container where all invitation cards will be added dynamically */
    private LinearLayout container;

    /** firestore instance for loading pending invitations */
    private FirebaseFirestore db;

    /** logged-in user's firestore id */
    private String entrantId;

    /**
     * initializes the page, sets up buttons, and loads the user's pending invitations.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_invitations);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("my invitations");
        }

        container = findViewById(R.id.container_invitations);
        db = FirebaseFirestore.getInstance();

        // get the currently logged-in entrant's id
        entrantId = FirebaseService.firebaseService.getCurrentUserId();

        if (entrantId == null || entrantId.isEmpty()) {
            showMessage("no logged-in user. please log in again.");
            return;
        }

        // button: guidelines
        Button guidelinesButton = findViewById(R.id.guidelines_button);
        if (guidelinesButton != null) {
            guidelinesButton.setOnClickListener(v ->
                    startActivity(new android.content.Intent(
                            EntrantInvitationsActivity.this,
                            GuidelinesActivity.class
                    ))
            );
        }

        // button: notifications
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
     * loads all invitations where this user has been marked as "selected".
     * every result becomes its own card with accept/decline functionality.
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
                        showMessage("you have no pending invitations.");
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String eventId = doc.getString(
                                DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD
                        );
                        addInvitationCard(doc.getId(), eventId);
                    }
                })
                .addOnFailureListener(e ->
                        showMessage("failed to load invitations: " + e.getMessage()));
    }

    /**
     * creates a small ui card representing one invitation.
     * it shows the event title, event id, and accept/decline buttons.
     *
     * @param docId   the id of the waitlist entry
     * @param eventId the id of the event this invite belongs to
     */
    private void addInvitationCard(String docId, String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            eventId = "(unknown event)";
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        card.setPadding(pad, pad, pad, pad);
        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        // title (will be replaced once event title loads)
        TextView title = new TextView(this);
        title.setTextSize(16f);

        // subtitle showing event id
        TextView subtitle = new TextView(this);
        subtitle.setTextSize(12f);
        subtitle.setText("event id: " + eventId);

        Button acceptBtn = new Button(this);
        acceptBtn.setText("accept");
        acceptBtn.setOnClickListener(v ->
                updateInvitationStatus(docId,
                        DatabaseConstants.ON_WAITING_LIST_STATUS.ACCEPTED,
                        card));

        Button declineBtn = new Button(this);
        declineBtn.setText("decline");
        declineBtn.setOnClickListener(v ->
                updateInvitationStatus(docId,
                        DatabaseConstants.ON_WAITING_LIST_STATUS.DECLINED,
                        card));

        card.addView(title);
        card.addView(subtitle);
        card.addView(acceptBtn);
        card.addView(declineBtn);

        container.addView(card);

        // fetch event title and update title text
        db.collection(DatabaseConstants.COLLECTION_EVENTS_NAME)
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    String eventTitle = null;
                    if (eventDoc != null && eventDoc.exists()) {
                        eventTitle = eventDoc.getString(
                                DatabaseConstants.COLLECTION_EVENTS_TITLE_FIELD
                        );
                    }
                    if (eventTitle == null || eventTitle.isEmpty()) {
                        eventTitle = "event";
                    }
                    title.setText("event: " + eventTitle);
                })
                .addOnFailureListener(e ->
                        title.setText("event: (failed to load)")
                );
    }

    /**
     * updates the status of an existing invitation (accepted or declined).
     * once done, the card is removed from the screen.
     *
     * @param docId     the waitlist document id
     * @param newStatus new status to apply (accepted or declined)
     * @param cardView  the card ui element that should be removed afterwards
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
                            "invitation " + newStatus.name().toLowerCase(),
                            Toast.LENGTH_SHORT).show();
                    container.removeView(cardView);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "failed to update: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    /**
     * small helper to show a message both as a toast and as fallback text on the screen
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

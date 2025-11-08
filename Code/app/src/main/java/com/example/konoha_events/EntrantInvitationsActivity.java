package com.example.konoha_events;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konoha_events.auth.EntrantDeviceIdStore;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import constants.DatabaseConstants;

public class EntrantInvitationsActivity extends AppCompatActivity {

    private LinearLayout container;
    private FirebaseFirestore db;
    private String entrantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_invitations);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Invitations");
        }

        container = findViewById(R.id.container_invitations);
        db = FirebaseFirestore.getInstance();

        // Use the same ID used for device based login
        entrantId = EntrantDeviceIdStore.getOrCreateId(this);

        if (entrantId == null || entrantId.isEmpty()) {
            showMessage("No entrant ID found. Please reopen the app.");
            return;
        }

        loadPendingInvitations();
    }

    private void loadPendingInvitations() {
        // Clear any old views
        container.removeAllViews();

        // Query: invitations for this entrant with PENDING status
        db.collection(DatabaseConstants.COLLECTION_ON_WAITING_LIST_NAME)
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_USER_ID_FIELD, entrantId)
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD,
                        DatabaseConstants.ON_WAITING_LIST_STATUS.PENDING.name())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        showMessage("You have no pending invitations.");
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        addInvitationCard(doc.getId(),
                                doc.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD));
                    }
                })
                .addOnFailureListener(e ->
                        showMessage("Failed to load invitations: " + e.getMessage()));
    }

    private void addInvitationCard(String docId, String eventId) {
        if (eventId == null) eventId = "(unknown event)";

        // Card layout
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        card.setPadding(pad, pad, pad, pad);
        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        // Event label
        TextView title = new TextView(this);
        title.setText("Event ID: " + eventId);
        title.setTextSize(16f);

        // Accept button
        Button acceptBtn = new Button(this);
        acceptBtn.setText("Accept");
        acceptBtn.setOnClickListener(v ->
                updateInvitationStatus(docId, DatabaseConstants.ON_WAITING_LIST_STATUS.ACCEPTED, card));

        // Decline button
        Button declineBtn = new Button(this);
        declineBtn.setText("Decline");
        declineBtn.setOnClickListener(v ->
                updateInvitationStatus(docId, DatabaseConstants.ON_WAITING_LIST_STATUS.DECLINED, card));

        // Add views into card
        card.addView(title);
        card.addView(acceptBtn);
        card.addView(declineBtn);

        // Add card into container
        container.addView(card);
    }

    private void updateInvitationStatus(String docId,
                                        DatabaseConstants.ON_WAITING_LIST_STATUS newStatus,
                                        View cardView) {

        db.collection(DatabaseConstants.COLLECTION_ON_WAITING_LIST_NAME)
                .document(docId)
                .update(DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD, newStatus.name())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Invitation " + newStatus.name().toLowerCase(),
                            Toast.LENGTH_SHORT).show();
                    // Removes the card from the UI
                    container.removeView(cardView);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to update: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        if (container.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText(msg);
            container.addView(empty);
        }
    }
}
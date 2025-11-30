package com.example.konoha_events;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import services.FirebaseService;

/**
 * activity that shows all notifications for the currently logged-in entrant.
 * it also lets the user turn app notifications on or off using a simple switch.
 */
public class EntrantNotificationsActivity extends AppCompatActivity {

    /** toggle that controls whether the user wants to receive notifications or not */
    private Switch switchNotifications;

    /** recyclerview that displays the list of notifications */
    private RecyclerView rvNotifications;

    /** text shown when the user has no notifications at all */
    private TextView tvNoNotifications;

    /** adapter backing the recyclerview with notification data */
    private NotificationsAdapter adapter;

    /** in-memory list of notifications loaded from firestore */
    private List<Notification> notificationList = new ArrayList<>();

    private FirebaseFirestore db;

    /** firestore user id for the currently logged-in entrant */
    private String entrantId;

    /**
     * sets up the notifications screen, loads the user's notification preference,
     * and fetches their notifications from firestore.
     *
     * @param savedInstanceState previous state, usually not super important here
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_notifications);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
        }

        db = FirebaseFirestore.getInstance();
        entrantId = FirebaseService.firebaseService.getCurrentUserId();

        rvNotifications = findViewById(R.id.rv_notifications);
        tvNoNotifications = findViewById(R.id.tv_no_notifications);

        adapter = new NotificationsAdapter(notificationList);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        switchNotifications = findViewById(R.id.switch_notifications);

        FirebaseService fbs = FirebaseService.firebaseService;
        String userId = fbs.getCurrentUserId();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this,
                    "no logged-in user. please log in again.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // load the current opt-out flag so the switch matches what is stored in firestore
        fbs.getUserDocumentReference(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    Boolean optOut = doc.getBoolean("notificationsOptOut");
                    // default behavior: user receives notifications (switch is on)
                    boolean receiveNotifications = (optOut == null || !optOut);
                    switchNotifications.setChecked(receiveNotifications);
                })
                .addOnFailureListener(e ->
                        // if this fails, just assume notifications are on
                        switchNotifications.setChecked(true)
                );

        // whenever the user toggles the switch, update the flag on their user document
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // isChecked = user wants notifications → optOut is the opposite
            boolean optOut = !isChecked;

            fbs.getUserDocumentReference(userId)
                    .update("notificationsOptOut", optOut)
                    .addOnSuccessListener(v ->
                            Log.i("EntrantNotifications",
                                    "updated notificationsOptOut=" + optOut))
                    .addOnFailureListener(e ->
                            Log.e("EntrantNotifications",
                                    "failed to update notificationsOptOut", e));
        });

        loadNotifications();
    }

    /**
     * loads all notifications for the current entrant from the "notifications" collection.
     * they are ordered by dateCreated in descending order so the newest ones show up first.
     * if there are none, a simple message is shown instead of the list.
     */
    private void loadNotifications() {
        db.collection("notifications")
                .whereEqualTo("userId", entrantId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    notificationList.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Notification notif = doc.toObject(Notification.class);
                        if (notif != null) {
                            notif.setId(doc.getId());
                            notificationList.add(notif);
                        }
                    }

                    adapter.updateData(notificationList);

                    if (notificationList.isEmpty()) {
                        tvNoNotifications.setVisibility(View.VISIBLE);
                    } else {
                        tvNoNotifications.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "failed to load notifications: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}

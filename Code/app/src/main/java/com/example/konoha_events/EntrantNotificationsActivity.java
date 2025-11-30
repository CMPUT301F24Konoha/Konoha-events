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

public class EntrantNotificationsActivity extends AppCompatActivity {
    private Switch switchNotifications;
    private RecyclerView rvNotifications;
    private TextView tvNoNotifications;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();

    private FirebaseFirestore db;
    private String entrantId;

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
            Toast.makeText(this, "No logged-in user. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

// 1) Load current preference from Firestore
        fbs.getUserDocumentReference(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    Boolean optOut = doc.getBoolean("notificationsOptOut");
                    // default: receive notifications (switch ON)
                    boolean receiveNotifications = (optOut == null || !optOut);
                    switchNotifications.setChecked(receiveNotifications);
                })
                .addOnFailureListener(e -> {
                    // If it fails, default to ON
                    switchNotifications.setChecked(true);
                });

// 2) Save when user toggles
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // isChecked = wants to receive notifications
            boolean optOut = !isChecked;

            fbs.getUserDocumentReference(userId)
                    .update("notificationsOptOut", optOut)
                    .addOnSuccessListener(v ->
                            Log.i("EntrantNotifications", "Updated notificationsOptOut=" + optOut))
                    .addOnFailureListener(e ->
                            Log.e("EntrantNotifications", "Failed to update notificationsOptOut", e));
        });


        loadNotifications();
    }

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
                                "Failed to load notifications: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}

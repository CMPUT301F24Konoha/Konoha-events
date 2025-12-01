package com.example.konoha_events;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationService {

    private static final String COLLECTION_NOTIFICATIONS = "notifications";

    /**
     * Sends a notification document to Firestore for a given user.
     *
     * @param userId           the entrant's Firestore userId
     * @param eventId          related event id (can be null)
     * @param notificationType e.g. "LOTTERY_WIN", "LOTTERY_LOSE", "INFO"
     * @param message          the message to show on the notifications screen
     */
    public static void sendNotification(String userId,
                                        String eventId,
                                        String notificationType,
                                        String message) {

        if (userId == null || userId.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("eventId", eventId);
        data.put("notificationType", notificationType);
        data.put("message", message);
        data.put("dateCreated", Timestamp.now());

        db.collection(COLLECTION_NOTIFICATIONS)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    // Optional: log or debug
                })
                .addOnFailureListener(e -> {
                    // Optional: log error
                });
    }
}

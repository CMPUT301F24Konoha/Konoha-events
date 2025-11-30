package com.example.konoha_events;

import com.google.firebase.Timestamp;

public class Notification {
    private String id;               // Firestore doc ID (we'll set this manually)
    private String userId;
    private String eventId;
    private String notificationType; // e.g. "INFO", "LOTTERY_WIN", "LOTTERY_LOSE"
    private String message;          // actual text shown to user
    private Timestamp dateCreated;   // when it was created

    public Notification() {
        // empty constructor needed for Firestore
    }

    // getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getEventId() { return eventId; }
    public String getNotificationType() { return notificationType; }
    public String getMessage() { return message; }
    public Timestamp getDateCreated() { return dateCreated; }

    // setter just for id (doc id)
    public void setId(String id) { this.id = id; }
}

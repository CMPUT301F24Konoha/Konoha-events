package com.example.konoha_events;

import com.google.firebase.Timestamp;

/**
 * basic data model for a single notification stored in firestore.
 * holds info like which user it belongs to, what event it relates to,
 * the message text, and when it was created.
 *
 * this class also plays nice with firestore since it has
 * an empty constructor and public getters.
 */
public class Notification {

    /** firestore document id (set manually after fetching) */
    private String id;

    /** id of the user who should receive this notification */
    private String userId;

    /** optional event id that this notification is tied to */
    private String eventId;

    /** simple label for the type, like "info" or "lottery_win" */
    private String notificationType;

    /** the actual message that shows up in the ui */
    private String message;

    /** timestamp of when the notification was created in firestore */
    private Timestamp dateCreated;

    /**
     * empty constructor required by firestore's automatic data mapping.
     * don't remove this or firestore will complain.
     */
    public Notification() { }

    // getters (kept short and simple to match the rest of the app)

    public String getId() { return id; }

    public String getUserId() { return userId; }

    public String getEventId() { return eventId; }

    public String getNotificationType() { return notificationType; }

    public String getMessage() { return message; }

    public Timestamp getDateCreated() { return dateCreated; }

    /**
     * setter only for the id since firestore doesn't auto-fill the doc id
     * when mapping objects, so we assign it manually after fetching.
     */
    public void setId(String id) { this.id = id; }
}

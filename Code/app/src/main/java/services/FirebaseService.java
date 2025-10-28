package services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import constants.DatabaseConstants;

/**
 * Class providing methods to interact with the database.
 */
public class FirebaseService {
    private final String LOG_TAG = "[FirebaseService]";
    public static FirebaseService firebaseService;
    private final CollectionReference events;
    private final CollectionReference users;
    private final CollectionReference onWaitingList;

    // Initializes the FirebaseService singleton instance, must be called before using the instance
    public static void init() {
        firebaseService = new FirebaseService();
    }

    public FirebaseService() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        events = db.collection(DatabaseConstants.COLLECTION_EVENTS_NAME);
        users = db.collection(DatabaseConstants.COLLECTION_USERS_NAME);
        onWaitingList = db.collection(DatabaseConstants.COLLECTION_ON_WAITING_LIST_NAME);
    }

    /**
     * Checks if a user with the given username and password pair exists in the database. If it does,
     * returns the user type of that user. If it doesn't, returns NULL user type.
     * @param username  The username of the user being logged in
     * @param password  The password of the user being logged in
     * @return          The user type of the logged in user, or NULL if no such user exists
     */
    public DatabaseConstants.USER_TYPE login(@NonNull String username, @NonNull String password) {
        List<DocumentSnapshot> documentSnapshots = users
                .whereEqualTo(DatabaseConstants.COLLECTION_USERS_USERNAME_FIELD, username)
                .whereEqualTo(DatabaseConstants.COLLECTION_USERS_PASSWORD_FIELD, password)
                .get()
                .getResult()
                .getDocuments();

        if (documentSnapshots.isEmpty()) {
            Log.i(LOG_TAG,
                    String.format("Database contains no entry for given username and password: %s, %s", username, password));
            return DatabaseConstants.USER_TYPE.NULL;
        }
        if (documentSnapshots.size() > 1) {
            Log.w(LOG_TAG,
                    String.format("Database contains multiple entries for given username and password: %s, %s", username, password));
        }

        String userTypeStr = documentSnapshots.get(0).getString(DatabaseConstants.COLLECTION_USERS_USER_TYPE_FIELD);
        if (userTypeStr == null) {
            Log.w(LOG_TAG,
                    String.format("Database contains entry with NULL user type. It should never be null. Username and password: %s, %s", username, password));
            return DatabaseConstants.USER_TYPE.NULL;
        }
        Log.i(LOG_TAG,
                String.format("Successfully logged in user %s, password %s, with user type %s", username, password, userTypeStr));
        return DatabaseConstants.USER_TYPE.valueOf(userTypeStr);
    }

    /**
     * Deletes a user with the given user ID from the database.
     * @param userId    The ID of the user to be deleted
     */
    public void deleteUser(@NonNull String userId) {
        users.document(userId)
                .delete()
                .addOnSuccessListener((v) -> Log.i(LOG_TAG,
                        String.format("Deleted user %s successfully", userId)))
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Didn't find or failed to delete user %s", userId)));
    }

    /**
     * Deletes an event with the given event ID from the database.
     * @param eventId   The ID of the event to be deleted
     */
    public void deleteEvent(@NonNull String eventId) {
        events.document(eventId)
                .delete()
                .addOnSuccessListener((v) -> Log.i(LOG_TAG,
                        String.format("Deleted event %s successfully", eventId)))
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Didn't find or failed to delete event %s", eventId)));
    }

    /**
     * Deletes an on waiting list entry with the given ID from the database.
     * @param onWaitingListId    The ID of the on waiting list entry to be deleted
     */
    public void deleteOnWaitingList(@NonNull String onWaitingListId) {
        onWaitingList.document(onWaitingListId)
                .delete()
                .addOnSuccessListener((v) -> Log.i(LOG_TAG,
                        String.format("Deleted on waiting list %s successfully", onWaitingListId)))
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Didn't find or failed to delete on waiting list %s", onWaitingListId)));
    }

    /**
     * Updates the image data of an event with the given event ID in the database. Can be used to
     * both update and remove the image data of an event.
     * @param eventId   The ID of the event to be updated
     * @param imageData The new image data of the event. If null, the image data will be removed
     */
    public void updateEventImage(@NonNull String eventId, @Nullable String imageData) {
        events.document(eventId)
                .update(DatabaseConstants.COLLECTION_EVENTS_IMAGE_DATA_FIELD, imageData)
                .addOnSuccessListener((v) -> {
                    if (imageData == null) {
                        Log.i(LOG_TAG,
                                String.format("Removed event image of event %s successfully", eventId));
                    }
                    Log.i(LOG_TAG,
                            String.format("Updated event image of event %s successfully", eventId));
                })
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Didn't find or failed to update image of event %s", eventId)));
    }
}

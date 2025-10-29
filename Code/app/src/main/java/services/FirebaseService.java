package services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.DatabaseConstants;
import lombok.Getter;
import models.EventModel;
import interfaces.UserTypeCallback;
import models.OnWaitingListModel;
import models.UserModel;
import util.ModelUtil;

/**
 * Class providing methods to interact with the database.
 */
public class FirebaseService {
    private final String LOG_TAG = "[FirebaseService]";
    public static FirebaseService firebaseService;
    private final CollectionReference events;
    private final CollectionReference users;
    private final CollectionReference onWaitingList;

    @Getter
    private final MutableLiveData<ArrayList<EventModel>> eventsLiveData;
    @Getter
    private final MutableLiveData<ArrayList<UserModel>> usersLiveData;
    @Getter
    private final MutableLiveData<ArrayList<OnWaitingListModel>> onWaitingListLiveData;

    // Initializes the FirebaseService singleton instance, must be called before using the instance
    public static void init() {
        firebaseService = new FirebaseService();
    }

    public FirebaseService() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        events = db.collection(DatabaseConstants.COLLECTION_EVENTS_NAME);
        users = db.collection(DatabaseConstants.COLLECTION_USERS_NAME);
        onWaitingList = db.collection(DatabaseConstants.COLLECTION_ON_WAITING_LIST_NAME);

        eventsLiveData = new MutableLiveData<>();
        usersLiveData = new MutableLiveData<>();
        onWaitingListLiveData = new MutableLiveData<>();

        setupListeners();
    }

    /**
     * Returns the DocumentReference of a user with the given user ID.
     * Use for when we only care about the data of one user changing. Attach a listener to this
     * reference and convert it into a user model when data changes.
     * @param userId    The ID of the user
     * @return          The DocumentReference of the user
     */
    public DocumentReference getUserDocumentReference(@NonNull String userId) {
        return users.document(userId);
    }

    /**
     * Returns the DocumentReference of an event with the given event ID.
     * Use for when we only care about the data of one event changing. Attach a listener to this
     * reference and convert it into an event model when data changes.
     * @param eventId   The ID of the event
     * @return          The DocumentReference of the event
     */
    public DocumentReference getEventDocumentReference(@NonNull String eventId) {
        return events.document(eventId);
    }

    /**
     * Creates a new user in the database with the given parameters.
     * @param userType The desired user type of the new user
     * @param username The desired username of the new user
     * @param password The desired password of the new user
     * @param deviceId The device ID of the new user, can be null
     */
    public void createUser(@NonNull DatabaseConstants.USER_TYPE userType,
                      @NonNull String username, @NonNull String password,
                      @Nullable String deviceId) {
        // Potentially add check for duplicate username

        Map<String, Object> userData = new HashMap<>();
        userData.put(DatabaseConstants.COLLECTION_USERS_USER_TYPE_FIELD, userType.name());
        userData.put(DatabaseConstants.COLLECTION_USERS_USERNAME_FIELD, username);
        userData.put(DatabaseConstants.COLLECTION_USERS_PASSWORD_FIELD, password);
        userData.put(DatabaseConstants.COLLECTION_USERS_DEVICE_ID_FIELD, deviceId);

        users.add(userData)
                .addOnSuccessListener((v) -> Log.i(LOG_TAG,
                        String.format("Created user %s successfully", username)))
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Didn't create user %s", username)));
    }

    /**
     * Checks if a user with the given username and password pair exists in the database. If it does,
     * returns the user type of that user. If it doesn't, returns NULL user type.
     * @param username  The username of the user being logged in
     * @param password  The password of the user being logged in
     * @param userTypeCallback   The callback to be called when the login is completed
     */
    public void login(@NonNull String username, @NonNull String password,
                      @NonNull UserTypeCallback userTypeCallback) {
        users
                .whereEqualTo(DatabaseConstants.COLLECTION_USERS_USERNAME_FIELD, username)
                .whereEqualTo(DatabaseConstants.COLLECTION_USERS_PASSWORD_FIELD, password)
                .get()
                .addOnSuccessListener((v) -> {
                    List<DocumentSnapshot> documentSnapshots = v.getDocuments();
                    if (documentSnapshots.isEmpty()) {
                        Log.i(LOG_TAG,
                                String.format("Database contains no entry for given username and password: %s, %s", username, password));
                        userTypeCallback.onCompleted(DatabaseConstants.USER_TYPE.NULL);
                        return;
                    }

                    if (documentSnapshots.size() > 1) {
                        Log.w(LOG_TAG,
                                String.format("Database contains multiple entries for given username and password: %s, %s", username, password));
                    }

                    String userTypeStr = documentSnapshots.get(0).getString(DatabaseConstants.COLLECTION_USERS_USER_TYPE_FIELD);
                    if (userTypeStr == null) {
                        Log.w(LOG_TAG,
                                String.format("Database contains entry with NULL user type. It should never be null. Username and password: %s, %s", username, password));
                        userTypeCallback.onCompleted(DatabaseConstants.USER_TYPE.NULL);
                        return;
                    }
                    Log.i(LOG_TAG,
                            String.format("Successfully logged in user %s, password %s, with user type %s", username, password, userTypeStr));
                    userTypeCallback.onCompleted(DatabaseConstants.USER_TYPE.valueOf(userTypeStr));
                })
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Failed to login: %s", e)));
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

    /**
     * Sets up the listeners for the collections in the database. Whenever a change in the db occurs,
     * the data will get updated.
     */
    private void setupListeners() {
        events.addSnapshotListener((data, error) -> {
            if (data != null) {
                ArrayList<EventModel> eventModels = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : data.getDocuments()) {
                    EventModel eventModel = ModelUtil.toEventModel(documentSnapshot);
                    eventModels.add(eventModel);
                }
                eventsLiveData.postValue(eventModels);
            }
        });

        users.addSnapshotListener((data, error) -> {
            if (data != null) {
                ArrayList<UserModel> userModels = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : data.getDocuments()) {
                    UserModel userModel = ModelUtil.toUserModel(documentSnapshot);
                    userModels.add(userModel);
                }
                usersLiveData.postValue(userModels);
            }
        });

        onWaitingList.addSnapshotListener((data, error) -> {
            if (data != null) {
                ArrayList<OnWaitingListModel> onWaitingListModels = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : data.getDocuments()) {
                    OnWaitingListModel onWaitingListModel = ModelUtil.toOnWaitingListModel(documentSnapshot);
                    onWaitingListModels.add(onWaitingListModel);
                }
                onWaitingListLiveData.postValue(onWaitingListModels);
            }
        });
    }
}

package services;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.DatabaseConstants;
import interfaces.UserTypeCallback;
import lombok.Getter;
import models.EventModel;
import models.OnWaitingListModel;
import models.UserModel;
import util.ModelUtil;
import util.QRCodeUtil;

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

    private String currentUserId;
    public String getCurrentUserId() { return currentUserId; }

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
     */
    public DocumentReference getUserDocumentReference(@NonNull String userId) {
        return users.document(userId);
    }

    /**
     * Returns the DocumentReference of an event with the given event ID.
     */
    public DocumentReference getEventDocumentReference(@NonNull String eventId) {
        return events.document(eventId);
    }

    /**
     * Creates a new user in the database with the given parameters.
     */
    public void createUser(@NonNull DatabaseConstants.USER_TYPE userType,
                           @NonNull String username, @NonNull String password,
                           @Nullable String deviceId) {

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
     * Checks if a user with the given username and password pair exists in the database.
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

                    DocumentSnapshot doc = documentSnapshots.get(0);
                    currentUserId = doc.getId();

                    String userTypeStr = documentSnapshots.get(0).getString(DatabaseConstants.COLLECTION_USERS_USER_TYPE_FIELD);
                    if (userTypeStr == null) {
                        Log.w(LOG_TAG,
                                String.format("Database entry with NULL user type. Username: %s", username));
                        userTypeCallback.onCompleted(DatabaseConstants.USER_TYPE.NULL);
                        return;
                    }
                    Log.i(LOG_TAG,
                            String.format("Successfully logged in user %s, type %s", username, userTypeStr));
                    userTypeCallback.onCompleted(DatabaseConstants.USER_TYPE.valueOf(userTypeStr));
                })
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Failed to login: %s", e)));
    }

    /**
     * Deletes a user with the given user ID from the database.
     */
    public void deleteUser(@NonNull String userId) {
        users.document(userId)
                .delete()
                .addOnSuccessListener((v) -> Log.i(LOG_TAG,
                        String.format("Deleted user %s successfully", userId)))
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Failed to delete user %s", userId)));
    }

    /**
     * Creates an event with a given organizer Id (device Id)
     */
    public void createEvent(@NonNull String deviceId,
                            @NonNull int entrantLimit,
                            @NonNull Date registrationDeadline,
                            @NonNull String eventTitle,
                            @NonNull String description,
                            @Nullable Uri imageUri,
                            boolean geolocationRequired) {

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(DatabaseConstants.COLLECTION_USERS_DEVICE_ID_FIELD, deviceId);
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_TITLE_FIELD, eventTitle);
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_DESCRIPTION_FIELD, description);
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_ENTRANT_LIMIT_FIELD, entrantLimit);
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_REGISTRATION_DEADLINE_FIELD, new Timestamp(registrationDeadline));
        eventData.put("geolocationRequired", geolocationRequired); // ✅ Added flag

        if (imageUri != null) {
            eventData.put(DatabaseConstants.COLLECTION_EVENTS_IMAGE_DATA_FIELD, imageUri.toString());
        }

        events.add(eventData)
                .addOnSuccessListener((documentReference) -> {
                    String eventId = documentReference.getId();
                    String qrCodeData = QRCodeUtil.generateQRCodeData(eventId);

                    documentReference.update(DatabaseConstants.COLLECTION_EVENTS_QR_CODE_DATA_FIELD, qrCodeData)
                            .addOnSuccessListener(v -> {
                                Log.i(LOG_TAG, String.format("Created event %s with QR code successfully", eventId));
                                if (imageUri != null) {
                                    uploadEventImage(eventId, imageUri);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(LOG_TAG, "Failed to add QR code to event", e);
                            });
                })
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Failed to create event %s", eventTitle)));
    }

    /**
     * Uploads an event poster image to Firebase Storage.
     */
    private void uploadEventImage(@NonNull String eventId, @NonNull Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("event_posters/" + eventId + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        events.document(eventId)
                                .update(DatabaseConstants.COLLECTION_EVENTS_IMAGE_DATA_FIELD, downloadUrl)
                                .addOnSuccessListener(v ->
                                        Log.i(LOG_TAG, "Event poster uploaded successfully"))
                                .addOnFailureListener(e ->
                                        Log.e(LOG_TAG, "Failed to update image URL", e));
                    });
                })
                .addOnFailureListener(e ->
                        Log.e(LOG_TAG, "Failed to upload event poster", e));
    }

    public void deleteEvent(@NonNull String eventId) {
        events.document(eventId)
                .delete()
                .addOnSuccessListener((v) -> Log.i(LOG_TAG,
                        String.format("Deleted event %s successfully", eventId)))
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Failed to delete event %s", eventId)));
    }

    public void deleteOnWaitingList(@NonNull String onWaitingListId) {
        onWaitingList.document(onWaitingListId)
                .delete()
                .addOnSuccessListener((v) -> Log.i(LOG_TAG,
                        String.format("Deleted on waiting list %s successfully", onWaitingListId)))
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Failed to delete on waiting list %s", onWaitingListId)));
    }

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
                        String.format("Failed to update image of event %s", eventId)));
    }

    /**
     * Sets up snapshot listeners for Firestore collections.
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
    /**
     *Joins the waiting list in firebase
     * @param eventId ID of the event
     * @param userId ID of the user trying to join the event.
     */
    public void joinWaitingList(@NonNull String eventId, @NonNull String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD, eventId);
        data.put(DatabaseConstants.COLLECTION_ON_WAITING_LIST_USER_ID_FIELD, userId);
        data.put(DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD,
                //Change pending if that's not correct, I would assume there would be one for waiting but there wasn't when I checked last
                DatabaseConstants.ON_WAITING_LIST_STATUS.PENDING.name());

        onWaitingList.add(data)
                .addOnSuccessListener(ref -> Log.i(LOG_TAG, "Joined waiting list: " + ref.getId()))
                .addOnFailureListener(e -> Log.e(LOG_TAG, "Failed to join waiting list", e));
    }
    /**
     *Leave the waiting list in firebase
     * @param eventId ID of the event
     * @param userId ID of the user trying to join the event.
     */
    public void leaveWaitingList(@NonNull String eventId, @NonNull String userId) {
        onWaitingList
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD, eventId)
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_USER_ID_FIELD, userId)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        Log.i(LOG_TAG, "No waitlist entry to remove for eventId=" + eventId + ", userId=" + userId);
                        return;
                    }
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        doc.getReference().delete()
                                .addOnSuccessListener(v -> Log.i(LOG_TAG, "Removed waitlist entry: " + doc.getId()))
                                .addOnFailureListener(e -> Log.e(LOG_TAG, "Failed to remove waitlist entry: " + doc.getId(), e));
                    }
                })
                .addOnFailureListener(e -> Log.e(LOG_TAG, "Query failed in leaveWaitingList", e));
    }

}

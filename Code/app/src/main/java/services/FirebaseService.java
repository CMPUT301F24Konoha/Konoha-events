package services;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import constants.DatabaseConstants;
import interfaces.BooleanCallback;
import interfaces.OnWaitingListArrayListCallback;
import interfaces.OnWaitingListCallback;
import interfaces.UserModelArrayListCallback;
import interfaces.UserTypeCallback;
import lombok.Data;
import lombok.Getter;
import models.EventModel;
import models.NotificationModel;
import models.OnWaitingListModel;
import models.UserModel;
import util.ConversionUtil;
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
    private final CollectionReference notifications;

    @Getter
    private final MutableLiveData<ArrayList<EventModel>> eventsLiveData;
    @Getter
    private final MutableLiveData<ArrayList<UserModel>> usersLiveData;
    @Getter
    private final MutableLiveData<ArrayList<OnWaitingListModel>> onWaitingListLiveData;
    @Getter
    private final MutableLiveData<ArrayList<NotificationModel>> notificationsLiveData;
    @Getter
    private DatabaseConstants.USER_TYPE loggedInUserType;

    // Initializes the FirebaseService singleton instance, must be called before using the instance
    public static void init() {
        firebaseService = new FirebaseService();
    }

    @Getter
    private String currentUserId;

    public FirebaseService() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        events = db.collection(DatabaseConstants.COLLECTION_EVENTS_NAME);
        users = db.collection(DatabaseConstants.COLLECTION_USERS_NAME);
        onWaitingList = db.collection(DatabaseConstants.COLLECTION_ON_WAITING_LIST_NAME);
        notifications = db.collection(DatabaseConstants.COLLECTION_NOTIFICATIONS_NAME);

        eventsLiveData = new MutableLiveData<>();
        usersLiveData = new MutableLiveData<>();
        onWaitingListLiveData = new MutableLiveData<>();
        notificationsLiveData = new MutableLiveData<>();

        setupListeners();
    }

    // Extra constructor for mocking
    public FirebaseService(CollectionReference events,
                           CollectionReference users,
                           CollectionReference onWaitingList,
                           MutableLiveData<ArrayList<EventModel>> eventsLiveData,
                           MutableLiveData<ArrayList<UserModel>> usersLiveData,
                           MutableLiveData<ArrayList<OnWaitingListModel>> onWaitingListLiveData,
                           CollectionReference notifications,
                           MutableLiveData<ArrayList<NotificationModel>> notificationsLiveData) {
        this.events = events;
        this.users = users;
        this.onWaitingList = onWaitingList;

        this.eventsLiveData = eventsLiveData;
        this.usersLiveData = usersLiveData;
        this.onWaitingListLiveData = onWaitingListLiveData;

        this.notifications = notifications;
        this.notificationsLiveData = notificationsLiveData;
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
                      @Nullable String deviceId, @Nullable String fullName,
                      @Nullable String phoneNumber) {
        // Potentially add check for duplicate username

        Map<String, Object> userData = new HashMap<>();
        userData.put(DatabaseConstants.COLLECTION_USERS_USER_TYPE_FIELD, userType.name());
        userData.put(DatabaseConstants.COLLECTION_USERS_USERNAME_FIELD, username);
        userData.put(DatabaseConstants.COLLECTION_USERS_PASSWORD_FIELD, password);
        userData.put(DatabaseConstants.COLLECTION_USERS_DEVICE_ID_FIELD, deviceId);
        userData.put(DatabaseConstants.COLLECTION_USERS_FULL_NAME_FIELD, fullName);
        userData.put(DatabaseConstants.COLLECTION_USERS_PHONE_FIELD, phoneNumber);

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

                    DocumentSnapshot doc = documentSnapshots.get(0);
                    currentUserId = doc.getId();

                    String userTypeStr = documentSnapshots.get(0).getString(DatabaseConstants.COLLECTION_USERS_USER_TYPE_FIELD);
                    if (userTypeStr == null) {
                        Log.w(LOG_TAG,
                                String.format("Database contains entry with NULL user type. It should never be null. Username and password: %s, %s", username, password));
                        userTypeCallback.onCompleted(DatabaseConstants.USER_TYPE.NULL);
                        return;
                    }

                    loggedInUserType = DatabaseConstants.USER_TYPE.valueOf(userTypeStr);

                    Log.i(LOG_TAG,
                            String.format("Successfully logged in user %s, password %s, with user type %s", username, password, userTypeStr));
                    userTypeCallback.onCompleted(loggedInUserType);
                })
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Failed to login: %s", e)));
    }

    /**
     * Attempts to log in a user using only their deviceId.
     * If a user with this deviceId is found in the users collection,
     * currentUserId and loggedInUserType are set, and callback is called with true.
     * Otherwise, callback is called with false.
     * This is used for device-based auto login for entrants.
     */
    public void loginWithDeviceId(@NonNull String deviceId,
                                  @NonNull BooleanCallback callback) {
        if (deviceId.isEmpty()) {
            Log.i(LOG_TAG, "Device login failed: empty deviceId");
            callback.onCompleted(false);
            return;
        }

        users
                .whereEqualTo(DatabaseConstants.COLLECTION_USERS_DEVICE_ID_FIELD, deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener(v -> {
                    if (v.getDocuments().isEmpty()) {
                        Log.i(LOG_TAG,
                                "No user found for deviceId " + deviceId);
                        callback.onCompleted(false);
                        return;
                    }

                    DocumentSnapshot doc = v.getDocuments().get(0);
                    currentUserId = doc.getId();

                    String userTypeStr = doc.getString(
                            DatabaseConstants.COLLECTION_USERS_USER_TYPE_FIELD);
                    if (userTypeStr != null) {
                        try {
                            loggedInUserType = DatabaseConstants.USER_TYPE.valueOf(userTypeStr);
                        } catch (IllegalArgumentException e) {
                            Log.w(LOG_TAG,
                                    "Invalid userType for device login: " + userTypeStr);
                        }
                    }

                    Log.i(LOG_TAG,
                            "Device login succeeded for user " + currentUserId
                                    + " (deviceId=" + deviceId + ")");
                    callback.onCompleted(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG,
                            "Device login query failed for deviceId=" + deviceId, e);
                    callback.onCompleted(false);
                });
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
     * Creates an event with a given organizer Id (device Id)
     * @param deviceId      The ID of the organizer creating the event
     * @param entrantLimit  An optional limit on entrants for the given event
     * @param registrationDeadline     The last day of registration
     * @param eventTitle     The name of the event
     * @param imageUri     Image description for the event
     * @param description   A description of the event
     */
    public void createEvent(@NonNull String deviceId,
                            @NonNull int entrantLimit,
                            @NonNull Date registrationDeadline,
                            @NonNull String eventTitle,
                            @NonNull String description,
                            @Nullable Uri imageUri,
                            @NonNull String organizerId,
                            boolean geolocationRequired,
                            ContentResolver contentResolver) {
        String base64ImageString;
        try {
            base64ImageString = ConversionUtil.convertUriToBase64(imageUri, contentResolver);
        } catch (Exception e) {
            base64ImageString = null;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(DatabaseConstants.COLLECTION_USERS_DEVICE_ID_FIELD, deviceId);
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_ORGANIZER_ID_FIELD, organizerId);
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_TITLE_FIELD, eventTitle);
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_IMAGE_DATA_FIELD, base64ImageString);
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_ENTRANT_LIMIT_FIELD, entrantLimit);
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_REGISTRATION_DEADLINE_FIELD, new Timestamp(registrationDeadline));
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_DESCRIPTION_FIELD, description);
        eventData.put("geolocationRequired", geolocationRequired);

        events.add(eventData)
                .addOnSuccessListener((documentReference) -> {
                    String eventId = documentReference.getId();

                    // Generate unique QR code data for this event
                    String qrCodeData = QRCodeUtil.generateQRCodeData(eventId);

                    documentReference.update(DatabaseConstants.COLLECTION_EVENTS_QR_CODE_DATA_FIELD, qrCodeData)
                            .addOnSuccessListener(v -> {
                                Log.i(LOG_TAG, String.format("Created event %s with QR code successfully", eventId));
                            })
                            .addOnFailureListener(e -> {
                                Log.e(LOG_TAG, "Failed to add QR code to event", e);
                            });
                })
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Didn't create event %s", eventTitle)));
    }

    /**
     * Uploads an event poster image to Firebase Storage (US 02.04.01)
     * @param eventId The ID of the event
     * @param imageUri The URI of the image to upload
     */
    public void uploadEventImage(@NonNull String eventId, @NonNull Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("event_posters/" + eventId + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        // Update Firestore with image URL
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

    public void getUsersOfEventWithStatus(@NonNull String eventId,
                                          @NonNull DatabaseConstants.ON_WAITING_LIST_STATUS status,
                                          @NonNull UserModelArrayListCallback callback) {
        Log.i("[FirebaseService]", String.format("Called getusers of event with status %s", status));
        onWaitingList
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD,
                        eventId)
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD,
                        status.name())
                .get()
                .addOnSuccessListener((v) -> {
                    ArrayList<UserModel> userModels = new ArrayList<>();

                    int size = v.getDocuments().size();
                    AtomicReference<Integer> count = new AtomicReference<>(0);
                    for (DocumentSnapshot documentSnapshot : v.getDocuments()) {
                        OnWaitingListModel onWaitingListModel = ModelUtil.toOnWaitingListModel(documentSnapshot);
                        String userId = onWaitingListModel.getUserId();

                        users.document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    UserModel userModel = ModelUtil.toUserModel(userDoc);
                                    userModels.add(userModel);
                                    count.updateAndGet(v1 -> v1 + 1);
                                    if (count.get() == size) {
                                        callback.onCompleted(userModels);
                                    }
                                })
                                .addOnFailureListener(doc -> {
                                    count.updateAndGet(v1 -> v1 + 1);
                                });
                    }
                    callback.onCompleted(userModels);
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG,
                            String.format("Failed to get  models of event %s with status %s",
                                    eventId, status.name()));
                    ArrayList<UserModel> userModels = new ArrayList<>();
                    callback.onCompleted(userModels);
                });

    }

    public void selectUsersForEvent(@NonNull String eventId,
                                    int count) {
        getOnWaitingListsOfEvent(eventId, new OnWaitingListArrayListCallback() {
            @Override
            public void onCompleted(ArrayList<OnWaitingListModel> onWaitingListModels) {
                ArrayList<OnWaitingListModel> waitingWaitingListModels = new ArrayList<OnWaitingListModel>();
                for (OnWaitingListModel model: onWaitingListModels) {
                    if (model.getStatus() == DatabaseConstants.ON_WAITING_LIST_STATUS.WAITING) {
                        waitingWaitingListModels.add(model);
                    }
                }

                int waitingCount = waitingWaitingListModels.size();
                if (count >= waitingCount) {
                    for (OnWaitingListModel model: waitingWaitingListModels) {
                        updateStatusOfOnWaitingList(model.getId(),
                                DatabaseConstants.ON_WAITING_LIST_STATUS.SELECTED,
                                new BooleanCallback() {
                                    @Override
                                    public void onCompleted(boolean succeeded) {

                                    }
                                });
                    }
                    return;
                }

                Collections.shuffle(waitingWaitingListModels);
                for (int i = 0; i < count; i++) {
                    OnWaitingListModel model = waitingWaitingListModels.get(i);
                    updateStatusOfOnWaitingList(model.getId(),
                            DatabaseConstants.ON_WAITING_LIST_STATUS.SELECTED,
                            new BooleanCallback() {
                                @Override
                                public void onCompleted(boolean succeeded) {
                                    // Optional: handle success/failure
                                }
                            });
                }
            }
        });
    }

    public void updateStatusOfOnWaitingList(@NonNull String onWaitingListId,
                                            @NonNull DatabaseConstants.ON_WAITING_LIST_STATUS status,
                                            @NonNull BooleanCallback callback) {
        onWaitingList.document(onWaitingListId)
                .update(DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD, status)
                .addOnSuccessListener(v -> {
                    callback.onCompleted(true);
                    Log.i(LOG_TAG,
                            String.format("Successfully updated status of onWaitingList model %s", onWaitingListId));
                })
                .addOnFailureListener(e -> {
                    callback.onCompleted(false);
                    Log.e(LOG_TAG,
                            String.format("Failed to update status of onWaitingList model %s", onWaitingListId));
                });
    }

    public void getOnWaitingListsOfEvent(@NonNull String eventId,
                                        @NonNull OnWaitingListArrayListCallback callback) {
        onWaitingList
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD,
                        eventId)
                .get()
                .addOnSuccessListener((v) -> {
                    ArrayList<OnWaitingListModel> onWaitingListModels = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : v.getDocuments()) {
                        OnWaitingListModel onWaitingListModel = ModelUtil.toOnWaitingListModel(documentSnapshot);
                        onWaitingListModels.add(onWaitingListModel);
                    }
                    callback.onCompleted(onWaitingListModels);
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG,
                            String.format("Failed to get onWaitingList models of event %s", eventId));
                    ArrayList<OnWaitingListModel> onWaitingListModels = new ArrayList<>();
                    callback.onCompleted(onWaitingListModels);
                });
    }

    public void getOnWaitingList(@NonNull String eventId,
                                 @NonNull String userId,
                                 @NonNull OnWaitingListCallback callback) {
        onWaitingList
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD,
                        eventId)
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_USER_ID_FIELD,
                        userId)
                .get()
                .addOnSuccessListener((v) -> {
                    if (v.getDocuments().size() == 1) {
                        DocumentSnapshot snapshot = v.getDocuments().get(0);
                        OnWaitingListModel model = ModelUtil.toOnWaitingListModel(snapshot);
                        callback.onCompleted(model);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG,
                            String.format("Failed to get onWaitingList models of event %s", eventId));
                    ArrayList<OnWaitingListModel> onWaitingListModels = new ArrayList<>();
                    callback.onCompleted(null);
                });
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
     * Sets the image url of the event with the given ID to null effectively deleting it.
     * @param eventId The ID of the event to delete the image
     */
    public void deleteEventImage(@NonNull String eventId) {
        events.document(eventId)
                .update(DatabaseConstants.COLLECTION_EVENTS_IMAGE_DATA_FIELD, null)
                .addOnSuccessListener((v) -> Log.i(LOG_TAG,
                        String.format("Deleted event image of event %s successfully", eventId)))
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Didn't find or failed to delete image of event %s", eventId)));
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

    public void deleteNotification(@NonNull String notificationId) {
        notifications.document(notificationId)
                .delete()
                .addOnSuccessListener((v) -> Log.i(LOG_TAG,
                        String.format("Deleted on notification %s successfully", notificationId)))
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Didn't find or failed to delete notification %s", notificationId)));
    }

    public void createNotification(@NonNull String eventId,
                                   @NonNull String userId,
                                   @NonNull String message,
                                   @NonNull DatabaseConstants.NOTIFICATION_TYPE type) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put(DatabaseConstants.COLLECTION_NOTIFICATIONS_EVENT_ID_FIELD, eventId);
        notificationData.put(DatabaseConstants.COLLECTION_NOTIFICATIONS_USER_ID_FIELD, userId);
        notificationData.put(DatabaseConstants.COLLECTION_NOTIFICATIONS_MESSAGE_FIELD, message);
        notificationData.put(DatabaseConstants.COLLECTION_NOTIFICATIONS_TYPE_FIELD, type.name());
        notificationData.put(DatabaseConstants.COLLECTION_NOTIFICATIONS_DATE_CREATED_FIELD, new Timestamp(new Date()));

        notifications.add(notificationData)
                .addOnSuccessListener((v) -> Log.i(LOG_TAG,
                        String.format("Created notification for user %s successfully", userId)))
                .addOnFailureListener((e) -> Log.i(LOG_TAG,
                        String.format("Failed to create notification for user %s", userId)));
    }

    public void createNotificationForUsersOfStatusOfEvent(@NonNull String eventId,
                                                        @NonNull DatabaseConstants.ON_WAITING_LIST_STATUS onWaitingListStatus,
                                                        @NonNull String message,
                                                        @NonNull DatabaseConstants.NOTIFICATION_TYPE type) {
        onWaitingList
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD, eventId)
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD, onWaitingListStatus.name())
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        String userId = doc.getString("userId");
                        if (userId != null) {
                            createNotification(eventId, userId, message, type);
                        }
                    }

                    Log.i(LOG_TAG, "Created notifications for "
                            + query.size() + " users of status " + onWaitingListStatus);
                })
                .addOnFailureListener(e ->
                        Log.e(LOG_TAG, "Failed to query waiting list for event " + eventId, e)
                );
    }

    public void createNotificationForAllUsersOfEvent(@NonNull String eventId,
                                                  @NonNull String message,
                                                  @NonNull DatabaseConstants.NOTIFICATION_TYPE type) {
        onWaitingList
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD, eventId)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        String userId = doc.getString("userId");
                        if (userId != null) {
                            createNotification(eventId, userId, message, type);
                        }
                    }

                    Log.i(LOG_TAG, "Created notifications for "
                            + query.size() + " users of event " + eventId);
                })
                .addOnFailureListener(e ->
                        Log.e(LOG_TAG, "Failed to query waiting list for event " + eventId, e)
                );
    }

    /**
     * Updates the image data of an event with the given event ID in the database. Can be used to
     * both update and remove the image data of an event.
     *
     * @param eventId         The ID of the event to be updated
     * @param imageData       The new image data of the event. If null, the image data will be removed
     * @param contentResolver
     */
    public void updateEventImage(@NonNull String eventId, @Nullable Uri imageData, ContentResolver contentResolver) {
        String base64String;
        try {
            base64String = ConversionUtil.convertUriToBase64(imageData, contentResolver);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error processing selected image: " + e.getMessage());
            return;
        }

        events.document(eventId)
                .update(DatabaseConstants.COLLECTION_EVENTS_IMAGE_DATA_FIELD, base64String)
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

        notifications.addSnapshotListener((data, error) -> {
            if (data != null) {
                ArrayList<NotificationModel> notificationModels = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : data.getDocuments()) {
                    NotificationModel notificationModel = ModelUtil.toNotificationModel(documentSnapshot);
                    notificationModels.add(notificationModel);
                }
                notificationsLiveData.postValue(notificationModels);
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
                DatabaseConstants.ON_WAITING_LIST_STATUS.WAITING.name());

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

    /**
     * Updates the deviceId field for the currently logged-in user in Firestore.
     * Used after a successful username/password login so future device logins work.
     */
    public void updateDeviceIdForCurrentUser(@NonNull String deviceId) {
        if (currentUserId == null || deviceId.isEmpty()) {
            Log.w(LOG_TAG, "updateDeviceIdForCurrentUser: missing currentUserId or deviceId");
            return;
        }

        users.document(currentUserId)
                .update(DatabaseConstants.COLLECTION_USERS_DEVICE_ID_FIELD, deviceId)
                .addOnSuccessListener(v ->
                        Log.i(LOG_TAG, "Updated deviceId for user " + currentUserId))
                .addOnFailureListener(e ->
                        Log.e(LOG_TAG, "Failed to update deviceId for user " + currentUserId, e));
    }
}

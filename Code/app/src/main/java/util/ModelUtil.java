package util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.sql.Time;
import java.util.Date;
import java.util.Objects;

import constants.DatabaseConstants;
import models.EventModel;
import models.NotificationModel;
import models.OnWaitingListModel;
import models.UserModel;

/**
 * Contains various helpers related to models.
 */
public class ModelUtil {
    private static String TAG = "[ModelUtil]";
    /**
     * Builds an EventModel from a DocumentSnapshot.
     * @param   documentSnapshot Snapshot of an event document
     * @return  EventModel built from the documentSnapshot
     */
        public static EventModel toEventModel(DocumentSnapshot documentSnapshot) {
            // Get the bitmap from base64 string of image
            String imageDataString = documentSnapshot.getString(DatabaseConstants.COLLECTION_EVENTS_IMAGE_DATA_FIELD);
            Bitmap imageBitmap = null;
            if (imageDataString != null && !imageDataString.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(imageDataString, Base64.DEFAULT);
                    imageBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                } catch (Exception e) {
                    Log.e(ModelUtil.TAG, "Failed to decode image data for event ID: " + documentSnapshot.getId());
                }
            }

            Long entrantLimitLong = documentSnapshot.getLong(DatabaseConstants.COLLECTION_EVENTS_ENTRANT_LIMIT_FIELD);
            Integer entrantLimit = entrantLimitLong != null ? entrantLimitLong.intValue() : null;

            Timestamp timestamp;
            Date registrationDeadline = null;
            try {
                timestamp = documentSnapshot.getTimestamp(DatabaseConstants.COLLECTION_EVENTS_REGISTRATION_DEADLINE_FIELD);
                registrationDeadline = timestamp != null ? timestamp.toDate() : null;
            } catch (Exception e) {
                Log.e(TAG, String.format("Failed to read timestamp for event Id: %s", documentSnapshot.getId()));
            }

            return EventModel.builder()
                    .id(documentSnapshot.getId())
                    .organizerId(Objects.requireNonNull(
                            documentSnapshot.getString(DatabaseConstants.COLLECTION_EVENTS_ORGANIZER_ID_FIELD)))
                    .imageBitmap(imageBitmap)
                    .eventTitle(documentSnapshot.getString(DatabaseConstants.COLLECTION_EVENTS_TITLE_FIELD))
                    .description(documentSnapshot.getString(DatabaseConstants.COLLECTION_EVENTS_DESCRIPTION_FIELD))
                    .deviceId(documentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_DEVICE_ID_FIELD))
                    .qrCodeData(documentSnapshot.getString(DatabaseConstants.COLLECTION_EVENTS_QR_CODE_DATA_FIELD))
                    .entrantLimit(entrantLimit)
                    .registrationDeadline(registrationDeadline)
                    .build();
        }

    /**
     * Builds a UserModel from a DocumentSnapshot.
     * @param   documentSnapshot Snapshot of a user document
     * @return  UserModel built from the documentSnapshot
     */
    public static UserModel toUserModel(DocumentSnapshot documentSnapshot) {
        return UserModel.builder()
                .id(documentSnapshot.getId())
                .username(Objects.requireNonNull(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_USERNAME_FIELD)))
                .password(Objects.requireNonNull(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_PASSWORD_FIELD)))
                .fullName(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_FULL_NAME_FIELD))
                .phoneNumber(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_PHONE_FIELD))
                .userType(DatabaseConstants.USER_TYPE.valueOf(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_USER_TYPE_FIELD)))
                .deviceId(documentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_DEVICE_ID_FIELD))
                .build();
    }

    /**
     * Builds an OnWaitingListModel from a DocumentSnapshot.
     * @param   documentSnapshot Snapshot of an on waiting list document
     * @return  OnWaitingListModel built from the documentSnapshot
     */
    public static OnWaitingListModel toOnWaitingListModel(DocumentSnapshot documentSnapshot) {
        String rawStatus = documentSnapshot.getString(
                DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD
        );

        DatabaseConstants.ON_WAITING_LIST_STATUS status = parseOnWaitingListStatus(rawStatus);

        return OnWaitingListModel.builder()
                .id(documentSnapshot.getId())
                .status(status)
                .userId(Objects.requireNonNull(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_USER_ID_FIELD)))
                .eventId(Objects.requireNonNull(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD)))
                .build();
    }

    public static NotificationModel toNotificationModel(DocumentSnapshot documentSnapshot) {
        Timestamp timestamp = documentSnapshot.getTimestamp(DatabaseConstants.COLLECTION_NOTIFICATIONS_DATE_CREATED_FIELD);
        Date dateCreated = timestamp != null ? timestamp.toDate() : null;

        return NotificationModel.builder()
                .id(documentSnapshot.getId())
                .userId(Objects.requireNonNull(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_NOTIFICATIONS_USER_ID_FIELD)))
                .eventId(Objects.requireNonNull(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_NOTIFICATIONS_EVENT_ID_FIELD)))
                .message(Objects.requireNonNull(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_NOTIFICATIONS_MESSAGE_FIELD)))
                .notificationType(DatabaseConstants.NOTIFICATION_TYPE.valueOf(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_NOTIFICATIONS_TYPE_FIELD)))
                .dateCreated(Objects.requireNonNull(dateCreated))
                .build();
    }

    private static DatabaseConstants.ON_WAITING_LIST_STATUS parseOnWaitingListStatus(String value) {
        if (value == null) {
            return DatabaseConstants.ON_WAITING_LIST_STATUS.WAITING;
        }

        try {
            return DatabaseConstants.ON_WAITING_LIST_STATUS.valueOf(value);
        } catch (IllegalArgumentException e) {
            // Handle legacy or shorthand values
            switch (value.toLowerCase()) {
                case "p":
                    return DatabaseConstants.ON_WAITING_LIST_STATUS.WAITING;
                case "a":
                    return DatabaseConstants.ON_WAITING_LIST_STATUS.ACCEPTED;
                case "d":
                    return DatabaseConstants.ON_WAITING_LIST_STATUS.DECLINED;
                case "c":
                    return DatabaseConstants.ON_WAITING_LIST_STATUS.CANCELLED;
                case "n":
                    return DatabaseConstants.ON_WAITING_LIST_STATUS.NULL;
                default:
                    return DatabaseConstants.ON_WAITING_LIST_STATUS.WAITING;
            }
        }
    }
}

package util;

import android.net.Uri;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.Objects;

import constants.DatabaseConstants;
import models.EventModel;
import models.OnWaitingListModel;
import models.UserModel;
public class ModelUtil {
    /**
     * Builds an EventModel from a DocumentSnapshot.
     * @param   documentSnapshot Snapshot of an event document
     * @return  EventModel built from the documentSnapshot
     */
    public static EventModel toEventModel(DocumentSnapshot documentSnapshot) {
        String imageDataString = documentSnapshot.getString(DatabaseConstants.COLLECTION_EVENTS_IMAGE_DATA_FIELD);
        Uri imageUri = null;
        if (imageDataString != null && !imageDataString.isEmpty()) {
            imageUri = Uri.parse(imageDataString);
        }

        Long entrantLimitLong = documentSnapshot.getLong(DatabaseConstants.COLLECTION_EVENTS_ENTRANT_LIMIT_FIELD);
        Integer entrantLimit = entrantLimitLong != null ? entrantLimitLong.intValue() : null;

        Timestamp timestamp = documentSnapshot.getTimestamp(DatabaseConstants.COLLECTION_EVENTS_REGISTRATION_DEADLINE_FIELD);
        Date registrationDeadline = timestamp != null ? timestamp.toDate() : null;

        return EventModel.builder()
                .id(documentSnapshot.getId())
                .imageUri(imageUri)
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
        return OnWaitingListModel.builder()
                .id(documentSnapshot.getId())
                .status(DatabaseConstants.ON_WAITING_LIST_STATUS.valueOf(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD)))
                .userId(Objects.requireNonNull(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_USER_ID_FIELD)))
                .eventId(Objects.requireNonNull(
                        documentSnapshot.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD)))
                .build();
    }
}

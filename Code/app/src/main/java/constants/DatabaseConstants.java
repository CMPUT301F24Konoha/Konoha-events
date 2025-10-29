package constants;

import androidx.annotation.NonNull;

public class DatabaseConstants {
    public static final String COLLECTION_EVENTS_NAME = "events";
    public static final String COLLECTION_EVENTS_IMAGE_DATA_FIELD = "imageData";
    public static final String COLLECTION_USERS_NAME = "users";
    public static final String COLLECTION_USERS_USERNAME_FIELD = "username";
    public static final String COLLECTION_USERS_PASSWORD_FIELD = "password";
    public static final String COLLECTION_USERS_USER_TYPE_FIELD = "userType";
    public static final String COLLECTION_USERS_DEVICE_ID_FIELD = "deviceId";
    public static final String COLLECTION_ON_WAITING_LIST_NAME = "onWaitingList";
    public static final String COLLECTION_ON_WAITING_LIST_USER_ID_FIELD = "userId";
    public static final String COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD = "eventId";


    public enum USER_TYPE {
        NULL,
        ENTRANT,
        ORGANIZER,
        ADMINISTRATOR;

        @NonNull
        @Override
        public String toString() {
            String lower = name().toLowerCase();
            return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        }
    }
    public enum ON_WAITING_LIST_STATUS {
        NULL,
        PENDING,
        ACCEPTED,
        DECLINED,
        CANCELLED
    }
}

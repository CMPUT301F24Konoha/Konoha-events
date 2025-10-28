package constants;

public class DatabaseConstants {
    public static final String COLLECTION_EVENTS_NAME = "events";
    public static final String COLLECTION_EVENTS_IMAGE_DATA_FIELD = "imageData";
    public static final String COLLECTION_USERS_NAME = "users";
    public static final String COLLECTION_USERS_USERNAME_FIELD = "username";
    public static final String COLLECTION_USERS_PASSWORD_FIELD = "password";
    public static final String COLLECTION_USERS_USER_TYPE_FIELD = "userType";
    public static final String COLLECTION_ON_WAITING_LIST_NAME = "onWaitingList";


    public enum USER_TYPE {
        NULL,
        ENTRANT,
        ORGANIZER,
        ADMINISTRATOR
    }
    public enum ON_WAITING_LIST_STATUS {
        NULL,
        PENDING,
        ACCEPTED,
        DECLINED,
        CANCELLED
    }
}

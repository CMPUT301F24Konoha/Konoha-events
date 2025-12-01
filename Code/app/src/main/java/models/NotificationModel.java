package models;

import java.util.Date;

import constants.DatabaseConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Model representing a notification to an entrant.
 */
@Getter
@AllArgsConstructor
@Builder
public class NotificationModel {
    @NonNull
    private String id;
    @NonNull
    private String eventId;
    @NonNull
    private String userId;
    @NonNull
    private String message;
    @NonNull
    private DatabaseConstants.NOTIFICATION_TYPE notificationType;
    @NonNull
    private Date dateCreated;
}

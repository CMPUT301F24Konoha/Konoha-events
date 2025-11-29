package models;

import constants.DatabaseConstants;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Model representing a user being on the waiting list of an event.
 * Contains different states representing the users status with the event.
 * More detailed description of states in the constants.
 */
@Getter
@AllArgsConstructor
@Builder
public class OnWaitingListModel {
    @NonNull
    private String id;
    @NonNull
    private DatabaseConstants.ON_WAITING_LIST_STATUS status;
    @NonNull
    private String userId;
    @NonNull
    private String eventId;
}

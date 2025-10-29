package models;

import constants.DatabaseConstants;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NonNull;

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

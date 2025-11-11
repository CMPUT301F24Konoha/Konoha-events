package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

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
}

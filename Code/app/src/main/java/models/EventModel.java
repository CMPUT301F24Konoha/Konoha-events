package models;

import android.net.Uri;

import androidx.annotation.Nullable;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import java.util.Date;
@Getter
@AllArgsConstructor
@Builder
public class EventModel {
    @NonNull
    private String id;

    @Nullable
    private String eventTitle;

    @Nullable
    private String description;

    // Organizer identification (using deviceId)
    @Nullable
    private String deviceId;

    // Images
    private Uri imageUri;

    // QR Code data - stores the unique identifier for the event
    @Nullable
    private String qrCodeData;

    @Nullable
    private Date registrationDeadline;

    @Nullable
    private Integer entrantLimit;

    /**
     * Check if registration is still open based on deadline
     */
    public boolean isRegistrationOpen() {
        if (registrationDeadline == null) {
            return true; // No deadline means always open
        }

        Date now = new Date();
        return now.before(registrationDeadline);
    }

    /**
     * Check if there's an entrant limit set
     */
    public boolean hasEntrantLimit() {
        return entrantLimit != null && entrantLimit > 0;
    }
}

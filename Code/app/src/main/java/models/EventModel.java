package models;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import interfaces.HasImage;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import java.util.Date;
@Getter
@AllArgsConstructor
@Builder
public class EventModel implements HasImage {
    @NonNull
    private String id;

    @NonNull
    private String organizerId;

    @Nullable
    private String eventTitle;

    @Nullable
    private String description;

    // Organizer identification (using deviceId)
    @Nullable
    private String deviceId;

    // Images
    @Nullable
    private Bitmap imageBitmap;

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

    /**
     * Getter method that returns the deviceId
     * @return deviceId the device Id
     */
    @Nullable
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Getter method that returns the ID of a given events uploader (the organizer)
     * @return the organizer ID
     */
    @Override
    public String getUploaderId() {
        return organizerId;
    }


    @Override
    public String getImageContext() {
        return "Event Image";
    }
}

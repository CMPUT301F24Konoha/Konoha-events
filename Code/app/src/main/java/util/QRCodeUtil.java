package util;

import androidx.annotation.NonNull;

public class QRCodeUtil {
    /**
     * Generates unique QR code data for an event (US 02.01.01)
     * Format: konoha://event/{eventId}
     */
    public static String generateQRCodeData(@NonNull String eventId) {
        return "konoha://event/" + eventId;
    }
}
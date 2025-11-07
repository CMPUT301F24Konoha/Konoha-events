package util;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * QRCodeUtil
 * ----------------------
 * Utility class for generating QR code data and bitmap images
 */
public class QRCodeUtil {

    /**
     * Generates unique QR code data for an event
     * Format: konoha://event/{eventId}
     */
    @NonNull
    public static String generateQRCodeData(@NonNull String eventId) {
        return "konoha://event/" + eventId;
    }

    /**
     * Extract event ID from scanned QR code data
     * Parses format: konoha://event/{eventId}
     *
     * @param qrCodeData The scanned QR code string
     * @return Event ID if valid format, null otherwise
     */
    public static String extractEventId(String qrCodeData) {
        if (qrCodeData == null || qrCodeData.isEmpty()) {
            return null;
        }

        // Check if it matches the format
        if (qrCodeData.startsWith("konoha://event/")) {
            return qrCodeData.substring("konoha://event/".length());
        }

        return qrCodeData;
    }

    /**
     * Generate a QR code bitmap from QR code data string
     *
     * @param qrCodeData The data to encode
     * @param size The size of the QR code in pixels (width and height)
     * @return Bitmap of the QR code
     */
    public static Bitmap generateQRCodeBitmap(String qrCodeData, int size) {
        if (qrCodeData == null || qrCodeData.isEmpty()) {
            return null;
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrCodeData, BarcodeFormat.QR_CODE, size, size);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate a QR code bitmap from event ID
     *
     * @param eventId The event ID
     * @param size The size of the QR code in pixels
     * @return Bitmap of the QR code
     */
    public static Bitmap generateQRCodeBitmapFromEventId(@NonNull String eventId, int size) {
        String qrCodeData = generateQRCodeData(eventId);
        return generateQRCodeBitmap(qrCodeData, size);
    }

    /**
     * Generate a QR code bitmap with default size of 512x512
     */
    public static Bitmap generateQRCodeBitmapFromEventId(@NonNull String eventId) {
        return generateQRCodeBitmapFromEventId(eventId, 512);
    }
}
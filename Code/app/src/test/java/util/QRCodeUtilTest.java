package util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for QRCode Util
 */
public class QRCodeUtilTest {
    /**
     * Tests if the ID of an event is properly extracted from the QR code
     */
    @Test
    public void testExtractEventIdFromQRCode() {
        String qrData = "konoha://event/testEvent";
        String eventId = QRCodeUtil.extractEventId(qrData);
        assertEquals("testEvent", eventId);
    }

    /**
     * Tests if a QR code is properly generated from an ID of a given event
     */
    @Test
    public void testGenerateQRCodeWithEventId() {
        String eventId = "testEvent";
        String qrData = QRCodeUtil.generateQRCodeData(eventId);
        assertEquals("konoha://event/testEvent", qrData);
    }
}

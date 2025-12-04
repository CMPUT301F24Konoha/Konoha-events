package com.example.konoha_events;

import static org.junit.Assert.*;
import org.junit.Test;

public class NotificationTest {

    @Test
    public void testDefaultConstructor_hasNullFields() {
        Notification n = new Notification();

        assertNull(n.getUserId());
        assertNull(n.getEventId());
        assertNull(n.getNotificationType());
        assertNull(n.getMessage());
        assertNull(n.getDateCreated());
    }

    @Test
    public void testSetId_updatesIdCorrectly() {
        Notification n = new Notification();

        n.setId("abc123");

        assertEquals("abc123", n.getId());
    }
}

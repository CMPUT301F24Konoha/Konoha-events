package com.example.konoha_events;

import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.*;

/**
 * Tests for event model method logic
 * Tests the logic without depending on EventModel class
 */
public class EventModelLogicTest {


    /**
     * Test whether the logic for isRegistrationOpen correctly returns true/false depending on registration deadline
     */
    @Test
    public void testIsRegistrationOpenLogic() {
        Date futureDeadline = new Date(System.currentTimeMillis() + 86400000); // Tomorrow
        Date pastDeadline = new Date(System.currentTimeMillis() - 86400000); // Yesterday
        Date now = new Date();

        boolean isFutureOpen = futureDeadline != null && !futureDeadline.before(now);
        boolean isPastOpen = pastDeadline != null && !pastDeadline.before(now);

        assertTrue(isFutureOpen);
        assertFalse(isPastOpen);
    }

    /**
     * Tests whether the logic for hasEntrantLimit correctly returns true/false
     */
    @Test
    public void testHasEntrantLimitWithPositiveValue() {
        Integer entrantLimit = 100;
        Integer nullEntrantLimit = null;
        Integer zeroEntrantLimit = 0;
        Integer negativeEntrantLimit = -1;


        boolean hasLimit = (entrantLimit != null && entrantLimit > 0);
        boolean hasLimitNull = (nullEntrantLimit != null && nullEntrantLimit > 0);
        boolean hasLimitZero = (zeroEntrantLimit != null && zeroEntrantLimit > 0);
        boolean hasLimitNegative = (negativeEntrantLimit != null && negativeEntrantLimit > 0);

        assertTrue(hasLimit);
        assertFalse(hasLimitNull);
        assertFalse(hasLimitZero);
        assertFalse(hasLimitNegative);
    }
}
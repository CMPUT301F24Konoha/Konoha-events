package com.example.konoha_events;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapterTest {

    @Test
    public void testItemCount_startsWithZero() {
        List<Notification> emptyList = new ArrayList<>();

        NotificationsAdapter adapter = new NotificationsAdapter(emptyList);

        // when created with an empty list, count should be zero
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testItemCount_matchesInitialListSize() {
        // build a list with two notifications
        List<Notification> list = new ArrayList<>();

        Notification n1 = new Notification();
        n1.setId("one");

        Notification n2 = new Notification();
        n2.setId("two");

        list.add(n1);
        list.add(n2);

        // create adapter with this list
        NotificationsAdapter adapter = new NotificationsAdapter(list);

        // item count should match list size
        assertEquals(2, adapter.getItemCount());
    }
}

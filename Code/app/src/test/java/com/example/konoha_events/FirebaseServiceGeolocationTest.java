package com.example.konoha_events;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;
import models.EventModel;
import models.NotificationModel;
import models.OnWaitingListModel;
import models.UserModel;
import services.FirebaseService;

public class FirebaseServiceGeolocationTest {

    @Mock CollectionReference mockEvents;
    @Mock CollectionReference mockUsers;
    @Mock CollectionReference mockOnWaitingList;
    @Mock CollectionReference mockNotifications;

    @Mock MutableLiveData<ArrayList<EventModel>> mockEventsLiveData;
    @Mock MutableLiveData<ArrayList<UserModel>> mockUsersLiveData;
    @Mock MutableLiveData<ArrayList<OnWaitingListModel>> mockOnWaitingListLiveData;
    @Mock MutableLiveData<ArrayList<NotificationModel>> mockNotificationsLiveData;

    @Mock ContentResolver mockContentResolver;

    // This will be returned by events.add(...)
    private Task<DocumentReference> mockAddTask;

    private FirebaseService firebaseService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Use the special constructor that takes mocked collections/live data
        firebaseService = new FirebaseService(
                mockEvents,
                mockUsers,
                mockOnWaitingList,
                mockEventsLiveData,
                mockUsersLiveData,
                mockOnWaitingListLiveData,
                mockNotifications,
                mockNotificationsLiveData
        );

        // Mock the Task returned by events.add(...)
        mockAddTask = Mockito.mock(Task.class);

        // When events.add(anyMap) is called, return our mock Task
        when(mockEvents.add(any())).thenReturn(mockAddTask);

        // Make addOnSuccessListener/addOnFailureListener safely return the same Task
        when(mockAddTask.addOnSuccessListener(any())).thenReturn(mockAddTask);
        when(mockAddTask.addOnFailureListener(any())).thenReturn(mockAddTask);
    }

    @Test
    public void createEvent_geolocationRequiredTrue_setsFieldTrue() {
        // Arrange
        String deviceId = "dev-123";
        int entrantLimit = 10;
        Date deadline = new Date();
        String title = "Test Event";
        String description = "Test Description";
        String organizerId = "org-1";
        boolean geolocationRequired = true;

        // Act
        firebaseService.createEvent(
                deviceId,
                entrantLimit,
                deadline,
                title,
                description,
                null,
                organizerId,
                geolocationRequired,
                mockContentResolver
        );

        // Assert: capture the map passed into events.add(...)
        ArgumentCaptor<Map<String, Object>> mapCaptor =
                ArgumentCaptor.forClass(Map.class);

        verify(mockEvents).add(mapCaptor.capture());

        Map<String, Object> savedData = mapCaptor.getValue();

        // geolocationRequired should be present and true
        assertEquals(Boolean.TRUE, savedData.get("geolocationRequired"));
    }

    @Test
    public void createEvent_geolocationRequiredFalse_setsFieldFalse() {
        // Arrange
        String deviceId = "dev-123";
        int entrantLimit = 10;
        Date deadline = new Date();
        String title = "Test Event 2";
        String description = "Test Description 2";
        String organizerId = "org-2";
        boolean geolocationRequired = false;

        // Act
        firebaseService.createEvent(
                deviceId,
                entrantLimit,
                deadline,
                title,
                description,
                null,
                organizerId,
                geolocationRequired,
                mockContentResolver
        );

        // Assert: capture the map passed into events.add(...)
        ArgumentCaptor<Map<String, Object>> mapCaptor =
                ArgumentCaptor.forClass(Map.class);

        verify(mockEvents).add(mapCaptor.capture());

        Map<String, Object> savedData = mapCaptor.getValue();

        // geolocationRequired should be present and false
        assertEquals(Boolean.FALSE, savedData.get("geolocationRequired"));
    }
}

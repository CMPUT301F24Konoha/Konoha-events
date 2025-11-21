package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import constants.DatabaseConstants;
import services.FirebaseService;
import util.ConversionUtil;
import util.QRCodeUtil;

public class FirebaseServiceTest {
    private FirebaseService firebaseService;
    private CollectionReference waitingList;


    @Before
    public void setup() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference users = FirebaseFirestore.getInstance().collection("users");
        CollectionReference events = FirebaseFirestore.getInstance().collection("events");
        //CollectionReference waitingList = FirebaseFirestore.getInstance().collection("onWaitingList");
        waitingList = db.collection(DatabaseConstants.COLLECTION_ON_WAITING_LIST_NAME);
        firebaseService = new FirebaseService(
                events,
                users,
                waitingList,
                new MutableLiveData<>(),
                new MutableLiveData<>(),
                new MutableLiveData<>()
        );
    }

    @Test
    public void givenRealUser_whenLogin_thenGetCorrectUserType() throws InterruptedException {
        String username = "testUser";
        String password = "1234";
        DatabaseConstants.USER_TYPE expectedType = DatabaseConstants.USER_TYPE.ORGANIZER;

        CountDownLatch latch = new CountDownLatch(1);

        firebaseService.login(username, password, userType -> {
            System.out.println("Returned user type: " + userType);
            assertEquals(expectedType, userType);
            latch.countDown();
        });

        if (!latch.await(10, TimeUnit.SECONDS)) {
            fail("Firebase login callback timed out");
        }
    }

    @Test
    public void givenInvalidUser_whenLogin_thenGetNullUserType() throws InterruptedException {
        String username = "";
        String password = "";

        CountDownLatch latch = new CountDownLatch(1);

        firebaseService.login(username, password, userType -> {
            System.out.println("Returned user type: " + userType);
            assertEquals(DatabaseConstants.USER_TYPE.NULL, userType);
            latch.countDown();
        });

        if (!latch.await(10, TimeUnit.SECONDS)) {
            fail("Firebase login callback timed out");
        }
    }

    @Test
    public void givenEventAndUser_whenJoinWaitingList_thenEntryCreated() throws InterruptedException {
        String eventId = "testEvent_join_" + System.currentTimeMillis();
        String userId = "testUser_join_" + System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(1);

        firebaseService.joinWaitingList(eventId, userId);

        waitingList.whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD, eventId)
                .whereEqualTo(DatabaseConstants.COLLECTION_ON_WAITING_LIST_USER_ID_FIELD, userId)
                .get()
                .addOnSuccessListener(qs -> {
                    try {
                        assertEquals("Expected exactly one waitlist entry", 1, qs.size());
                        DocumentSnapshot doc = qs.getDocuments().get(0);

                        String storedEventId = doc.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD);
                        String storedUserId = doc.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_USER_ID_FIELD);
                        String status = doc.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD);

                        assertEquals(eventId, storedEventId);
                        assertEquals(userId, storedUserId);
                        assertEquals(
                                DatabaseConstants.ON_WAITING_LIST_STATUS.WAITING.name(),
                                status
                        );
                    } finally {
                        latch.countDown();
                    }
                })
                .addOnFailureListener(e -> {
                    fail("Query failed in join waitlist test: " + e);
                    latch.countDown();
                });

        if (!latch.await(10, TimeUnit.SECONDS)) {
            fail("joinWaitingList test timed out");
        }
    }

    @Test
    public void createEventWithoutImage() {
        Map<String, Object> eventData = new HashMap<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference events = db.collection(DatabaseConstants.COLLECTION_EVENTS_NAME);

        Instant instant = Instant.now();
        long currentTimestamp = instant.toEpochMilli();

        eventData.put(DatabaseConstants.COLLECTION_USERS_DEVICE_ID_FIELD, "12345");
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_ORGANIZER_ID_FIELD, "12345");
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_TITLE_FIELD, "TEST EVENT");
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_ENTRANT_LIMIT_FIELD, 10);
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_REGISTRATION_DEADLINE_FIELD, currentTimestamp);
        eventData.put(DatabaseConstants.COLLECTION_EVENTS_DESCRIPTION_FIELD, "This is a test event");

        events.add(eventData)
                .addOnSuccessListener((documentReference) -> {
                    String eventId = documentReference.getId();

                    // Generate unique QR code data for this event
                    String qrCodeData = QRCodeUtil.generateQRCodeData(eventId);

                    documentReference.update(DatabaseConstants.COLLECTION_EVENTS_QR_CODE_DATA_FIELD, qrCodeData)
                            .addOnSuccessListener(v -> {
                                Log.i("Firebase Service Test", String.format("Created event %s with QR code successfully", eventId));
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firebase Service Test", "Failed to add QR code to event", e);
                            });
                })
                .addOnFailureListener((e) -> Log.i("Firebase Service Test",
                        String.format("Didn't create event %s")));
    }
}
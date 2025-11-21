package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import constants.DatabaseConstants;
import services.FirebaseService;

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
}
package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import constants.DatabaseConstants;
import services.FirebaseService;

public class FirebaseServiceTest {
    private FirebaseService firebaseService;

    @Before
    public void setup() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
        }

        CollectionReference users = FirebaseFirestore.getInstance().collection("users");
        CollectionReference events = FirebaseFirestore.getInstance().collection("events");
        CollectionReference waitingList = FirebaseFirestore.getInstance().collection("onWaitingList");

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
}

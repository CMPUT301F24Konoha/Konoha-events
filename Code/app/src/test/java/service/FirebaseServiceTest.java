package service;

import static org.mockito.Mockito.mock;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collection;

import models.EventModel;
import models.OnWaitingListModel;
import models.UserModel;
import services.FirebaseService;

public class FirebaseServiceTest {
    public FirebaseService fbs;
    public CollectionReference eventsMock;
    public CollectionReference usersMock;
    public CollectionReference waitingListMock;
    public MutableLiveData<ArrayList<EventModel>> liveEventModelDataMock;
    public MutableLiveData<ArrayList<UserModel>> liveUserModelDataMock;
    public MutableLiveData<ArrayList<OnWaitingListModel>> onWaitingListModelDataMock;

    @Before
    public void setup() {
        eventsMock = mock(CollectionReference.class);
        usersMock = mock(CollectionReference.class);
        waitingListMock = mock(CollectionReference.class);

        liveEventModelDataMock = mock(MutableLiveData.class);
        liveUserModelDataMock = mock(MutableLiveData.class);
        onWaitingListModelDataMock = mock(MutableLiveData.class);

        fbs = new FirebaseService(
                eventsMock,
                usersMock,
                waitingListMock,
                liveEventModelDataMock,
                liveUserModelDataMock,
                onWaitingListModelDataMock
        );
    }
}

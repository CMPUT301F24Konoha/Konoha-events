package util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import constants.DatabaseConstants;
import models.EventModel;
import models.NotificationModel;
import models.OnWaitingListModel;
import models.UserModel;

public class ModelUtilTest {
    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void toEventModel_with_validDocumentSnapshot_then_makeCorrectModel() {
        when(mockDocumentSnapshot.getId()).thenReturn("event123");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_EVENTS_ORGANIZER_ID_FIELD))
                .thenReturn("organizer456");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_EVENTS_IMAGE_DATA_FIELD))
                .thenReturn(null);
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_EVENTS_TITLE_FIELD))
                .thenReturn("Sample Event");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_EVENTS_DESCRIPTION_FIELD))
                .thenReturn("An example description");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_DEVICE_ID_FIELD))
                .thenReturn("device789");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_EVENTS_QR_CODE_DATA_FIELD))
                .thenReturn("qrdata");
        when(mockDocumentSnapshot.getLong(DatabaseConstants.COLLECTION_EVENTS_ENTRANT_LIMIT_FIELD))
                .thenReturn(10L);

        long seconds = 1893456000L;
        when(mockDocumentSnapshot.getTimestamp(DatabaseConstants.COLLECTION_EVENTS_REGISTRATION_DEADLINE_FIELD))
                .thenReturn(new Timestamp(seconds, 0));

        EventModel eventModelResult = ModelUtil.toEventModel(mockDocumentSnapshot);

        assertNotNull(eventModelResult);
        assertEquals("event123", eventModelResult.getId());
        assertEquals("organizer456", eventModelResult.getOrganizerId());
        assertNull(eventModelResult.getImageBitmap());
        assertEquals("Sample Event", eventModelResult.getEventTitle());
        assertEquals("An example description", eventModelResult.getDescription());
        assertEquals("device789", eventModelResult.getDeviceId());
        assertEquals("qrdata", eventModelResult.getQrCodeData());
        assertEquals(Integer.valueOf(10), eventModelResult.getEntrantLimit());

        Date expectedDate = new Date(seconds * 1000L);
        assertEquals(expectedDate, eventModelResult.getRegistrationDeadline());
    }

    @Test
    public void toNotificationModel_with_validDocumentSnapshot_then_makeCorrectModel() {
        when(mockDocumentSnapshot.getId()).thenReturn("notif123");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_NOTIFICATIONS_USER_ID_FIELD))
                .thenReturn("user123");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_NOTIFICATIONS_EVENT_ID_FIELD))
                .thenReturn("event123");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_NOTIFICATIONS_MESSAGE_FIELD))
                .thenReturn("You have been accepted");

        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_NOTIFICATIONS_TYPE_FIELD))
                .thenReturn(DatabaseConstants.NOTIFICATION_TYPE.values()[0].name());

        long seconds = 1609459200L;
        when(mockDocumentSnapshot.getTimestamp(DatabaseConstants.COLLECTION_NOTIFICATIONS_DATE_CREATED_FIELD))
                .thenReturn(new Timestamp(seconds, 0));

        NotificationModel notificationModel = ModelUtil.toNotificationModel(mockDocumentSnapshot);

        assertNotNull(notificationModel);
        assertEquals("notif123", notificationModel.getId());
        assertEquals("user123", notificationModel.getUserId());
        assertEquals("event123", notificationModel.getEventId());
        assertEquals("You have been accepted", notificationModel.getMessage());
        assertNotNull(notificationModel.getNotificationType());
        assertEquals(new Date(seconds * 1000L), notificationModel.getDateCreated());
    }

    @Test
    public void toOnWaitingListModel_with_validDocumentSnapshot_then_makeCorrectModel() {
        when(mockDocumentSnapshot.getId()).thenReturn("wait123");

        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_STATUS_FIELD))
                .thenReturn(DatabaseConstants.ON_WAITING_LIST_STATUS.values()[1].name());
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_USER_ID_FIELD))
                .thenReturn("user123");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_ON_WAITING_LIST_EVENT_ID_FIELD))
                .thenReturn("event123");

        OnWaitingListModel model = ModelUtil.toOnWaitingListModel(mockDocumentSnapshot);

        assertNotNull(model);
        assertEquals("wait123", model.getId());
        assertEquals("user123", model.getUserId());
        assertEquals("event123", model.getEventId());
        assertNotNull(model.getStatus());
    }

    @Test
    public void toUserModel_with_validDocumentSnapshot_then_makeCorrectModel() {
        when(mockDocumentSnapshot.getId()).thenReturn("user123");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_USERNAME_FIELD))
                .thenReturn("jdoe");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_PASSWORD_FIELD))
                .thenReturn("secret");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_FULL_NAME_FIELD))
                .thenReturn("John Doe");
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_PHONE_FIELD))
                .thenReturn("+123456789");

        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_USER_TYPE_FIELD))
                .thenReturn(DatabaseConstants.USER_TYPE.values()[0].name());
        when(mockDocumentSnapshot.getString(DatabaseConstants.COLLECTION_USERS_DEVICE_ID_FIELD))
                .thenReturn("device-abc");

        UserModel userModel = ModelUtil.toUserModel(mockDocumentSnapshot);

        assertNotNull(userModel);
        assertEquals("user123", userModel.getId());
        assertEquals("jdoe", userModel.getUsername());
        assertEquals("secret", userModel.getPassword());
        assertEquals("John Doe", userModel.getFullName());
        assertEquals("+123456789", userModel.getPhoneNumber());
        assertNotNull(userModel.getUserType());
        assertEquals("device-abc", userModel.getDeviceId());
    }
}

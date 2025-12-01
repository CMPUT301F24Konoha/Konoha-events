package views;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.konoha_events.R;

import java.util.ArrayList;

import lombok.NonNull;
import models.EventModel;
import models.NotificationModel;
import services.FirebaseService;
import util.ModelUtil;

/**
 * This is an adapter of NotificationModels to display info about each NotificationModel.
 * Displays various information about the notification and provides admin controls.
 * This view should only be seen only by admins.
 */
public class NotificationAdminDashboardView extends ArrayAdapter<NotificationModel> {
    private static final String tag = "[NotificationAdminDashboardView]";
    private FirebaseService fbs;
    private Class<? extends Activity> returnActivity;
    public NotificationAdminDashboardView(Context context, ArrayList<NotificationModel> notificationModels, Class<? extends Activity> returnActivity) {
        super(context, 0, notificationModels);
        this.returnActivity = returnActivity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        fbs = FirebaseService.firebaseService;

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.notification_admin_dashboard_view,
                    parent, false);
        } else {
            view = convertView;
        }

        NotificationModel notificationModel = getItem(position);
        if (notificationModel == null) {
            Log.e(tag, "Could not get notification model at position " + position);
            return view;
        }

        TextView dateCreatedTextView = view.findViewById(R.id.notification_admin_dashboard_view_date_created_text_view);
        TextView userIdTextView = view.findViewById(R.id.notification_admin_dashboard_view_user_id_text_view);
        TextView eventIdTextView = view.findViewById(R.id.notification_admin_dashboard_view_event_id_text_view);
        TextView organizerIdTextView = view.findViewById(R.id.notification_admin_dashboard_view_organizer_id_text_view);
        TextView messageTextView = view.findViewById(R.id.notification_admin_dashboard_view_message_text_view);
        Button deleteButton = view.findViewById(R.id.notification_admin_dashboard_view_delete_button);

        organizerIdTextView.setText("Loading...");
        fbs.getEventDocumentReference(notificationModel.getEventId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    EventModel eventModel = ModelUtil.toEventModel(documentSnapshot);
                    organizerIdTextView.setText(
                            String.format("OrganizerId: %s", eventModel.getOrganizerId()));
                })
                .addOnFailureListener(e -> {
                    Log.e(tag, String.format(
                            "Failed to get event: %s to display organizer for notification: %s", notificationModel.getEventId(), notificationModel.getId()));
                });

        dateCreatedTextView.setText(
                String.format("DateCreated: %s", notificationModel.getDateCreated().toString()));
        userIdTextView.setText(
                String.format("UserId: %s", notificationModel.getUserId()));
        eventIdTextView.setText(
                String.format("EventId: %s", notificationModel.getEventId()));
        messageTextView.setText(
                String.format("Message: %s", notificationModel.getMessage()));
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbs.deleteNotification(notificationModel.getId());
            }
        });


        return view;
    }
}

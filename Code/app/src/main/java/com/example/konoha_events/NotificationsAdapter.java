package com.example.konoha_events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * adapter for showing a list of entrant notifications inside a recyclerview.
 * it basically connects each notification to the ui so the user can scroll through them.
 * each row contains:
 * - a small title for the notification type
 * - the main message body
 * - the time it was created
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    /** the current list of notifications we want to show */
    private List<Notification> notifications;

    /** simple formatter used to turn the timestamp into something readable */
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * creates a new adapter with an initial list of notifications
     *
     * @param notifications the list to start with
     */
    public NotificationsAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    /**
     * inflates a single notification item layout when the recyclerview needs one
     */
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    /**
     * fills in the ui elements for one notification row based on its position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notif = notifications.get(position);

        // quick title using its type
        String title = notif.getNotificationType();
        if (title == null || title.isEmpty()) {
            title = "notification";
        }
        holder.titleText.setText(title);

        // set the body text
        holder.bodyText.setText(notif.getMessage());

        // apply formatted time if available
        if (notif.getDateCreated() != null) {
            String timeString = sdf.format(notif.getDateCreated().toDate());
            holder.timeText.setText(timeString);
        } else {
            holder.timeText.setText("");
        }
    }

    /**
     * @return how many notifications are currently stored in the adapter
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * replaces the old list with a new one and refreshes the view.
     * simple enough, nothing fancy.
     */
    public void updateData(List<Notification> newList) {
        this.notifications = newList;
        notifyDataSetChanged();
    }

    /**
     * small viewholder class holding references to the ui parts of one row,
     * so the adapter doesn't keep looking them up every time
     */
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView bodyText;
        TextView timeText;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tv_notification_title);
            bodyText = itemView.findViewById(R.id.tv_notification_body);
            timeText = itemView.findViewById(R.id.tv_notification_time);
        }
    }
}

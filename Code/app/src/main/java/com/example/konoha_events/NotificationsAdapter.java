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

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public NotificationsAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notif = notifications.get(position);

        // Use notificationType as a simple title (you can make this prettier later)
        String title = notif.getNotificationType();
        if (title == null || title.isEmpty()) {
            title = "Notification";
        }
        holder.titleText.setText(title);

        // Use message as body
        holder.bodyText.setText(notif.getMessage());

        if (notif.getDateCreated() != null) {
            String timeString = sdf.format(notif.getDateCreated().toDate());
            holder.timeText.setText(timeString);
        } else {
            holder.timeText.setText("");
        }
    }


    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateData(List<Notification> newList) {
        this.notifications = newList;
        notifyDataSetChanged();
    }

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

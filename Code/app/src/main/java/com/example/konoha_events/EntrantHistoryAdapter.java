package com.example.konoha_events;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import constants.DatabaseConstants;
import models.EventModel;
/**
 * Adapter that displays a list of events from the entrant's history.
 * Each list item includes the event and the user's waitlist status.
 */
public class EntrantHistoryAdapter extends RecyclerView.Adapter<EntrantHistoryAdapter.HistoryViewHolder> {
    //Most recycled code from EventsAdapter.
    /**
     * Represents a single history entry containing an event
     * and the user's status for that event.
     */
    public static class HistoryItem {
        public final EventModel event;
        public final DatabaseConstants.ON_WAITING_LIST_STATUS status;

        //Model the event in terms of event and user event status.
        /**
         * Creates a history entry.
         *
         * @param event  The event being shown.
         * @param status The user's waitlist status for that event.
         */
        public HistoryItem(EventModel event, DatabaseConstants.ON_WAITING_LIST_STATUS status) {
            this.event = event;
            this.status = status;
        }
    }

    private final List<HistoryItem> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    /**
     * Replaces the adapter's data with a new list of history items.
     *
     * @param newItems The new list to display.
     */
    public void submitList(List<HistoryItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }
    /**
     * Inflates the history item layout.
     *
     * @param parent   The parent view group.
     * @param viewType The type of view (unused here).
     * @return A new HistoryViewHolder.
     */
    @NonNull
    //Call the history event screen, history_item_event.xml. Please don't make UI changes here, adjust the view if possible.
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item_event, parent, false);
        return new HistoryViewHolder(v);
    }
    /**
     * Binds a history item to its view.
     *
     * @param h   The view holder to update.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder h, int position) {
        HistoryItem item = items.get(position);
        EventModel e = item.event;

        h.title.setText(e.getEventTitle() == null ? "" : e.getEventTitle());

        if (e.getRegistrationDeadline() != null) {
            h.deadline.setText("Registration deadline: " +
                    dateFormat.format(e.getRegistrationDeadline()));
        } else {
            h.deadline.setText("Registration always open");
        }

        Bitmap img = e.getImageBitmap();
        if (img != null) {
            h.image.setImageBitmap(img);
        } else {
            h.image.setImageResource(R.drawable.ic_launcher_background);
        }

        //Set the status text to a colour to make it nicer and more user friendly.
        String statusText = "";
        int color = Color.DKGRAY;

        if (item.status != null) {
            switch (item.status) {
                case ACCEPTED:
                    statusText = "ACCEPTED";
                    color = Color.parseColor("#388E3C"); // green
                    break;
                case DECLINED:
                    statusText = "DECLINED";
                    color = Color.parseColor("#FB8C00"); // orange
                    break;
                case CANCELLED:
                    statusText = "CANCELLED";
                    color = Color.parseColor("#D32F2F"); // red
                    break;
                case WAITING:
                    statusText = "WAITING";
                    color = Color.parseColor("#7B1FA2"); // purple
                    break;
                case SELECTED:
                    statusText = "SELECTED";
                    color = Color.parseColor("#1976D2"); // blue
                    break;
            }
        }

        h.status.setText(statusText);
        h.status.setTextColor(color);
    }
    /**
     * @return The number of items shown in the history list.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }
    /**
     * Holds references to the views inside a history list item.
     */
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView deadline;
        TextView status;
        /**
         * Creates a view holder for a history item.
         *
         * @param itemView The root view of the list item layout.
         */
        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageEvent);
            title = itemView.findViewById(R.id.textEventTitle);
            deadline = itemView.findViewById(R.id.textRegistrationDeadline);
            status = itemView.findViewById(R.id.textStatus);
        }
    }
}

package com.example.konoha_events;
import com.bumptech.glide.Glide;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import models.EventModel;
/**
 * Adapter used to display a list of events for the entrant.
 * Each row shows event information, an optional image,
 * a primary action button, and an info button for details.
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
    /**
     * Callback for events triggered from each row.
     */
    public interface Callback {
        /**
         * Called when the main button ("Join" or "Leave") is pressed.
         *
         * @param event The event associated with that row.
         */
        void onRowClick(EventModel event);
        /**
         * Called when the info button is pressed.
         *
         * @param event The event associated with that row.
         */
        void onQrClick(EventModel event);
    }

    private final List<EventModel> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private String primaryButtonLabel = "Join Waitlist";
    /**
     * Updates the button label shown on each row (e.g., "Join" or "Leave").
     *
     * @param label New label to use for the row's main button.
     */
    public void setPrimaryButtonLabel(String label) {
        if (label != null) primaryButtonLabel = label;
    }
    private final Callback callback;
    /**
     * Creates an adapter with a callback for row actions.
     *
     * @param callback The object that handles click events for each row.
     */
    public EventsAdapter(Callback callback) {
        this.callback = callback;
    }
    /**
     * Replaces the current event list with a new one.
     *
     * @param newItems The new list of events to display.
     */
    public void submitList(List<EventModel> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }
    /**
     * Inflates the XML layout for a single event row.
     *
     * @param parent   The parent view group.
     * @param viewType The view type (unused).
     * @return A new EventViewHolder.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(v);
    }
    /**
     * Binds event data to a row on screen.
     *
     * @param h   The view holder for the row.
     * @param position The position of the event in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder h, int position) {
        EventModel e = items.get(position);

        h.title.setText(e.getEventTitle() == null ? "" : e.getEventTitle());
        h.description.setText(e.getDescription() == null ? "" : e.getDescription());
        if (e.getRegistrationDeadline() != null) {
            h.deadline.setText("Registration deadline: " + dateFormat.format(e.getRegistrationDeadline()));
        } else {
            h.deadline.setText("Registration always open");
        }
        //Image uploading, some pictures are working. Not super familiar with glide so this can change if we need it to.
        Bitmap img = e.getImageBitmap();
        if (img != null) {
            Glide.with(h.image.getContext())
                    .load(img)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(h.image);
        } else {
            //set to default placeholder, can also change this I picked at random.
            h.image.setImageResource(R.drawable.ic_launcher_background);
        }

        //Set button text to join button, important for allowing the MyEvent view to change it to leave waitlist
        h.joinButton.setText(primaryButtonLabel);
        h.joinButton.setOnClickListener(v -> {
            if (callback != null) callback.onRowClick(e);
        });

        h.infoButton.setOnClickListener(v -> {
            if (callback != null) callback.onQrClick(e);
        });
    }

    /**
     * @return The number of events shown in the list.
     */
    @Override
    public int getItemCount() { return items.size(); }
    /**
     * Holds references to views inside a single event row.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        Button joinButton;
        ImageView image;
        TextView title, description, deadline;
        ImageButton infoButton;
        /**
         * Creates a view holder for an event row.
         *
         * @param itemView The root view of the row layout.
         */
        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageEvent);
            title = itemView.findViewById(R.id.textEventTitle);
            description = itemView.findViewById(R.id.textEventSubtitle);
            deadline = itemView.findViewById(R.id.textRegistrationDeadline);
            infoButton = itemView.findViewById(R.id.imageButton);
            joinButton = itemView.findViewById(R.id.buttonJoinWaitlist);

        }
    }
}

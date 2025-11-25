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

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    public interface Callback {
        void onRowClick(EventModel event);
        void onQrClick(EventModel event);
    }

    private final List<EventModel> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private String primaryButtonLabel = "Join Waitlist";

    public void setPrimaryButtonLabel(String label) {
        if (label != null) primaryButtonLabel = label;
    }
    private final Callback callback;
    public EventsAdapter(Callback callback) {
        this.callback = callback;
    }
    public void submitList(List<EventModel> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(v);
    }
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

        h.qrButton.setOnClickListener(v -> {
            if (callback != null) callback.onQrClick(e);
        });
    }


    @Override
    public int getItemCount() { return items.size(); }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        Button joinButton;
        ImageView image;
        TextView title, description, deadline;
        ImageButton qrButton;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageEvent);
            title = itemView.findViewById(R.id.textEventTitle);
            description = itemView.findViewById(R.id.textEventSubtitle);
            deadline = itemView.findViewById(R.id.textRegistrationDeadline);
            qrButton = itemView.findViewById(R.id.imageButton);
            joinButton = itemView.findViewById(R.id.buttonJoinWaitlist);

        }
    }
}

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

public class EntrantHistoryAdapter extends RecyclerView.Adapter<EntrantHistoryAdapter.HistoryViewHolder> {
    //Most recycled code from EventsAdapter.
    public static class HistoryItem {
        public final EventModel event;
        public final DatabaseConstants.ON_WAITING_LIST_STATUS status;

        //Model the event in terms of event and user event status.
        public HistoryItem(EventModel event, DatabaseConstants.ON_WAITING_LIST_STATUS status) {
            this.event = event;
            this.status = status;
        }
    }

    private final List<HistoryItem> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public void submitList(List<HistoryItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    //Call the history event screen, history_item_event.xml. Please don't make UI changes here, adjust the view if possible.
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item_event, parent, false);
        return new HistoryViewHolder(v);
    }

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

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView deadline;
        TextView status;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageEvent);
            title = itemView.findViewById(R.id.textEventTitle);
            deadline = itemView.findViewById(R.id.textRegistrationDeadline);
            status = itemView.findViewById(R.id.textStatus);
        }
    }
}

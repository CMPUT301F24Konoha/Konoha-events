package views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.konoha_events.EventDetails;
import com.example.konoha_events.R;

import java.util.ArrayList;

import constants.IntentConstants;
import models.EventModel;
import services.FirebaseService;

public class EventAdminDashboardView extends ArrayAdapter<EventModel> {
    private static final String tag = "[EventAdminDashboardView]";
    private FirebaseService fbs;
    private Class<? extends Activity> returnActivity;

    public EventAdminDashboardView(Context context, ArrayList<EventModel> eventModels, Class<? extends Activity> returnActivity) {
        super(context, 0, eventModels);
        this.returnActivity = returnActivity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        fbs = FirebaseService.firebaseService;

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.user_event_dashboard_view,
                    parent, false);
        } else {
            view = convertView;
        }

        EventModel eventModel =  getItem(position);
        if (eventModel == null) {
            Log.e(tag, "Could not get event model at position " + position);
            return view;
        }

        TextView eventNameTextView = view.findViewById(R.id.event_admin_dashboard_view_name);
        TextView eventIdTextView = view.findViewById(R.id.event_admin_dashboard_view_id);
        TextView eventDeadlineTextView = view.findViewById(R.id.event_admin_dashboard_view_deadline);
        TextView eventOnWaitingListCountTextView = view.findViewById(R.id.event_admin_dashboard_view_on_waiting_list_count);
        TextView eventDescriptionTextView = view.findViewById(R.id.event_admin_dashboard_view_description);
        Button removeEventButton = view.findViewById(R.id.event_admin_dashboard_view_remove_button);
        Button editDetailsButton = view.findViewById(R.id.event_admin_dashboard_view_details_button);
        ImageView posterView = view.findViewById(R.id.event_admin_dashboard_image_view);

        if (eventModel.getImageBitmap() != null) {
            posterView.setVisibility(View.VISIBLE);
            Glide.with(getContext())
                    .load(eventModel.getImageBitmap())
                    .into(posterView);
        } else {
            Glide.with(getContext()).clear(posterView);
            posterView.setImageDrawable(null);
            posterView.setVisibility(View.GONE);
        }

        eventNameTextView.setText(eventModel.getEventTitle());
        eventIdTextView.setText("ID: " + eventModel.getId());

        String eventDeadlineString;
        if (eventModel.getRegistrationDeadline() == null) {
            eventDeadlineString = "Deadline: N/A";
        } else {
            eventDeadlineString = "Deadline: " + eventModel.getRegistrationDeadline().toString();
        }
        eventDeadlineTextView.setText(eventDeadlineString);

        eventOnWaitingListCountTextView.setText("Loading...");
        fbs.getOnWaitingListsOfEvent(eventModel.getId(), (onWaitingListModels) -> {
            int count = onWaitingListModels.size();
            String entrantsString;

            if (eventModel.getEntrantLimit() != null && eventModel.getEntrantLimit() != -1) {
                entrantsString = String.format("Total Entrants: (%s/%s)", count, eventModel.getEntrantLimit());
            } else {
                entrantsString = String.format("Total Entrants: %s", count);
            }
            eventOnWaitingListCountTextView.setText(entrantsString);
        });

        eventDescriptionTextView.setText("Description: " + eventModel.getDescription());

        editDetailsButton.setOnClickListener((v) -> {
            Context context = getContext();
            Intent intent = new Intent(context, EventDetails.class);
            intent.putExtra(IntentConstants.INTENT_VIEW_EVENT_CALLER_TYPE, returnActivity.getName());
            intent.putExtra(IntentConstants.INTENT_VIEW_EVENT_EVENT_ID, eventModel.getId());
            context.startActivity(intent);
        });

        removeEventButton.setOnClickListener((v) -> {
            fbs.deleteEvent(eventModel.getId());
        });

        return view;
    }
}
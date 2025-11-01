package views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.konoha_events.R;

import java.util.ArrayList;

import models.EventModel;
import models.UserModel;
import services.FirebaseService;

public class EventAdminDashboardView extends ArrayAdapter<EventModel> {
    private static final String tag = "[EventAdminDashboardView]";
    private FirebaseService fbs;

    public EventAdminDashboardView(Context context, ArrayList<EventModel> eventModels) {
        super(context, 0, eventModels);
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
        
        return view;
    }
}
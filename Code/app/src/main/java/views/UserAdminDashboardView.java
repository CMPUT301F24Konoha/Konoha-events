package views;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.konoha_events.R;

import java.util.ArrayList;

import constants.DatabaseConstants;
import models.EventModel;
import models.OnWaitingListModel;
import models.UserModel;
import services.FirebaseService;

/**
 * This view is no longer used exclusively for admin use. It is also used in the organizer event
 * view. Therefore in the future it should be named something more general. For now we will keep it
 * like this to prevent merge conflicts.
 */
public class UserAdminDashboardView extends ArrayAdapter<UserModel> {
    private static final String tag = "[UserAdminDashboardView]";
    private FirebaseService fbs;
    // This field should be non-null in the context of an Event
    private final String eventId;

    public UserAdminDashboardView(Context context,
                                  ArrayList<UserModel> userModels,
                                  @Nullable String eventId) {
        super(context, 0, userModels);
        this.eventId = eventId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        fbs = FirebaseService.firebaseService;

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.user_admin_dashboard_view, parent, false);
        }

        TextView nameTextView = view.findViewById(R.id.user_admin_dashboard_view_name);
        TextView emailTextView = view.findViewById(R.id.user_admin_dashboard_view_email);
        TextView locationTextView = view.findViewById(R.id.user_admin_dashboard_view_location);
        TextView detailsTextView = view.findViewById(R.id.user_admin_dashboard_view_details);
        Button removeProfileButton = view.findViewById(R.id.user_admin_dashboard_view_remove_button);
        Button cancelEntrantButton = view.findViewById(R.id.user_admin_dashboard_view_cancel_button);

        UserModel userModel = getItem(position);
        if (userModel == null) {
            Log.e(tag, "Could not get user model at position " + position);
            return view;
        }

        nameTextView.setText(String.format("%s (%s)", userModel.getFullName(), userModel.getUserType()));
        emailTextView.setVisibility(GONE);
        locationTextView.setVisibility(GONE);
        detailsTextView.setVisibility(GONE);

        if (fbs.getLoggedInUserType() == DatabaseConstants.USER_TYPE.ADMINISTRATOR) {
            removeProfileButton.setVisibility(VISIBLE);
            removeProfileButton.setOnClickListener(v -> fbs.deleteUser(userModel.getId()));
        } else {
            removeProfileButton.setVisibility(GONE);
        }

        cancelEntrantButton.setVisibility(GONE);
        if (eventId != null) {
            fbs.getOnWaitingList(eventId, userModel.getId(), onWaitingListModel -> {
                if (onWaitingListModel == null || !onWaitingListModel.getUserId().equals(userModel.getId())) {
                    return;
                }

                cancelEntrantButton.setVisibility(VISIBLE);
                switch (onWaitingListModel.getStatus()) {
                    case WAITING:
                    case SELECTED:
                    case ACCEPTED:
                    case DECLINED:
                        cancelEntrantButton.setText("Cancel Entrant");
                        cancelEntrantButton.setOnClickListener(v -> fbs.updateStatusOfOnWaitingList(
                                onWaitingListModel.getId(),
                                DatabaseConstants.ON_WAITING_LIST_STATUS.CANCELLED,
                                b -> {}
                        ));
                        break;

                    case CANCELLED:
                        cancelEntrantButton.setText("Move to Waiting");
                        cancelEntrantButton.setOnClickListener(v -> fbs.updateStatusOfOnWaitingList(
                                onWaitingListModel.getId(),
                                DatabaseConstants.ON_WAITING_LIST_STATUS.WAITING,
                                b -> {}
                        ));
                        break;
                }
                cancelEntrantButton.setVisibility(VISIBLE);
            });
        }

        return view;
    }
}

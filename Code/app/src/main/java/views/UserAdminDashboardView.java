package views;

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

import models.UserModel;
import services.FirebaseService;

public class UserAdminDashboardView extends ArrayAdapter<UserModel> {
    private static final String tag = "[UserAdminDashboardView]";
    private FirebaseService fbs;
    private TextView nameTextView;
    // Below fields do not yet exist in the UserModel, should be added in the future.
    private TextView emailTextView;
    private TextView locationTextView;
    private TextView detailsTextView;
    private Button removeProfileButton;

    public UserAdminDashboardView(Context context, ArrayList<UserModel> userModels) {
        super(context, 0, userModels);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        fbs = FirebaseService.firebaseService;

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.user_admin_dashboard_view,
                    parent, false);
        } else {
            view = convertView;
        }
        nameTextView = view.findViewById(R.id.user_admin_dashboard_view_name);
        emailTextView = view.findViewById(R.id.user_admin_dashboard_view_email);
        locationTextView = view.findViewById(R.id.user_admin_dashboard_view_location);
        detailsTextView = view.findViewById(R.id.user_admin_dashboard_view_details);
        removeProfileButton = view.findViewById(R.id.user_admin_dashboard_view_remove_button);


        UserModel userModel = getItem(position);
        if (userModel == null) {
            Log.e(tag, "Could not get user model at position " + position);
            return view;
        }

        nameTextView.setText(String.format(
                "%s (%s)", userModel.getUsername(), userModel.getUserType()));
        // We should also set other fields here once they're added to user model.

        removeProfileButton.setOnClickListener((v) -> {
            fbs.deleteUser(userModel.getId());
        });

        return view;
    }
}

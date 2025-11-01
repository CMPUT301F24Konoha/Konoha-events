package views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.konoha_events.R;

import java.util.ArrayList;

import interfaces.HasImage;
import models.EventModel;
import services.FirebaseService;

public class ImageAdminDashboardView extends ArrayAdapter<HasImage> {
    private static final String tag = "[EventAdminDashboardView]";
    private FirebaseService fbs;
    private ImageView imageView;
    private Button removeImageButton;

    public ImageAdminDashboardView(Context context, ArrayList<HasImage> imageUrls) {
        super(context, 0, imageUrls);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        fbs = FirebaseService.firebaseService;

        HasImage hasImage = getItem(position);
        if (hasImage == null || hasImage.getImageUrl() == null) {
            Log.e(tag, "Could not get hasImage model at position " + position);
            View emptyView = new View(getContext());
            emptyView.setLayoutParams(new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT, 0));
            emptyView.setVisibility(View.GONE);
            return emptyView;
        }

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.user_image_dashboard_view,
                    parent, false);
        } else {
            view = convertView;
        }

        imageView = view.findViewById(R.id.image_admin_dashboard_view_image);
        removeImageButton = view.findViewById(R.id.image_admin_dashboard_view_remove_button);

        Glide.with(getContext())
                .load(hasImage.getImageUrl())
                .into(imageView);

        removeImageButton.setOnClickListener((v) -> {
            if (hasImage instanceof EventModel) {
                fbs.deleteEventImage(hasImage.getId());
            } else {
                Log.e(tag, "Removed image called on class without delete function.");
            }
        });

        return view;
    }
}

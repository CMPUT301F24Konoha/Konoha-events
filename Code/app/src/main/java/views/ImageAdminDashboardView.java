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

/**
 * This is an adapter of classes implementing HasImage to display the various images of the system.
 * Displays various information about the context of the image and provides admin controls.
 * This view should only be seen only by admins.
 */
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
        if (hasImage == null) {
            Log.e(tag, "Could not get hasImage model at position " + position);
            View emptyView = new View(getContext());
            emptyView.setLayoutParams(new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT, 0));
            emptyView.setVisibility(View.GONE);
            return emptyView;
        }

        if (hasImage.getImageBitmap() == null) {
            // No log because it's okay to not have an image
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
                .load(hasImage.getImageBitmap())
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

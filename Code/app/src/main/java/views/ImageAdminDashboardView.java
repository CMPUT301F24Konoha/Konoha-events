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
import android.widget.TextView;

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
    private TextView uploaderTextView;
    private ImageView imageView;
    private Button removeImageButton;

    public ImageAdminDashboardView(Context context, ArrayList<HasImage> imageUrls) {
        super(context, 0, imageUrls);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        HasImage hasImage = getItem(position);

        // Skip entries without an image
        if (hasImage == null || hasImage.getImageBitmap() == null) {
            View emptyView = new View(getContext());
            emptyView.setLayoutParams(new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT, 0));
            emptyView.setVisibility(View.GONE);
            return emptyView;
        }

        View view = convertView;
        if (view == null || view.findViewById(R.id.image_admin_dashboard_view_image) == null) {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.user_image_dashboard_view, parent, false);
        }

        TextView uploaderTextView = view.findViewById(R.id.image_admin_dashboard_view_image_uploader_text);
        TextView imageContextTextView = view.findViewById(R.id.image_admin_dashboard_view_image_context_text);
        ImageView imageView = view.findViewById(R.id.image_admin_dashboard_view_image);
        Button removeImageButton = view.findViewById(R.id.image_admin_dashboard_view_remove_button);

        imageContextTextView.setText(String.format("Image Context: %s", hasImage.getImageContext()));
        uploaderTextView.setText(String.format("Uploader: %s", hasImage.getUploaderId()));

        Glide.with(getContext())
                .load(hasImage.getImageBitmap())
                .into(imageView);

        removeImageButton.setOnClickListener((v) -> {
            if (hasImage instanceof EventModel) {
                FirebaseService.firebaseService.deleteEventImage(hasImage.getId());
            }
        });

        return view;
    }
}

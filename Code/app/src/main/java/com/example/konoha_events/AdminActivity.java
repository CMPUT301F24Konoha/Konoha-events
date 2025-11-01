package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * AdminActivity
 * ----------------------
 * Displays the Admin Dashboard UI.
 * Provides navigation options to manage:
 * - Events
 * - Profiles
 * - Organizers
 * - Images
 * - Notifications
 *
 * NOTE: Only UI skeleton and click placeholders are implemented.
 * The actual logic will be handled by other group members.
 */

public class AdminActivity extends AppCompatActivity {

    private Button manageEventsButton, manageProfilesButton, manageOrganizersButton,
            manageImagesButton, manageNotificationsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize buttons
        manageEventsButton = findViewById(R.id.manageEventsButton);
        manageProfilesButton = findViewById(R.id.manageProfilesButton);
        manageOrganizersButton = findViewById(R.id.manageOrganizersButton);
        manageImagesButton = findViewById(R.id.manageImagesButton);
        manageNotificationsButton = findViewById(R.id.manageNotificationsButton);

        // TODO: Replace Toasts with Intent navigation once sub-pages exist
        manageEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminEventActivity.class);
            startActivity(intent);
        });

        manageProfilesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });

        manageOrganizersButton.setOnClickListener(v ->
                Toast.makeText(this, "Manage Organizers screen (to be implemented)", Toast.LENGTH_SHORT).show());

        manageImagesButton.setOnClickListener(v ->
                Toast.makeText(this, "Manage Images screen (to be implemented)", Toast.LENGTH_SHORT).show());

        manageNotificationsButton.setOnClickListener(v ->
                Toast.makeText(this, "Manage Notifications screen (to be implemented)", Toast.LENGTH_SHORT).show());
    }
}

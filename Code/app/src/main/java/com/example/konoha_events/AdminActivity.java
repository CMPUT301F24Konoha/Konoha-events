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
 * - Images
 * - Notifications
 *
 * NOTE: Only UI skeleton and click placeholders are implemented.
 * The actual logic will be handled by other group members.
 */

public class AdminActivity extends AppCompatActivity {

    private Button manageEventsButton, manageProfilesButton,
            manageImagesButton, manageNotificationsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize buttons
        manageEventsButton = findViewById(R.id.manageEventsButton);
        manageProfilesButton = findViewById(R.id.manageProfilesButton);
        manageImagesButton = findViewById(R.id.manageImagesButton);
        manageNotificationsButton = findViewById(R.id.manageNotificationsButton);

        manageEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminEventActivity.class);
            startActivity(intent);
        });

        manageProfilesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });

        manageImagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminImageActivity.class);
            startActivity(intent);
        });

        manageNotificationsButton.setOnClickListener(v ->
                Toast.makeText(this, "Manage Notifications screen (to be implemented)", Toast.LENGTH_SHORT).show());
    }
}

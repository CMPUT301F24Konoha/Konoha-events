package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konoha_events.auth.EntrantDeviceIdStore;

/**
 * EntrantHomeActivity
 * -----------------------------------
 * Home screen for entrant users identified by device ID.
 * Provides navigation to view guidelines and invitations.
 * Provides access to QR code scanner and other entrant features.
 * Device location is now handled in LoginActivity, not here.
 */
public class EntrantHomeActivity extends AppCompatActivity {

    private Button scanQRButton, viewEventsButton, profileButton;
    private TextView deviceIdText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_home);

        // Retrieve or create the device ID
        String deviceId = EntrantDeviceIdStore.getOrCreateId(this);

        // Basic UI
        TextView tv = new TextView(this);
        tv.setText("Entrant Home\n\nDevice ID:\n" + (deviceId == null ? "(none yet)" : deviceId));
        tv.setTextSize(18f);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad, pad, pad);
        setContentView(tv);

        // Create layout programmatically
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(48, 48, 48, 48);

        // Title
        TextView title = new TextView(this);
        title.setText("Entrant Home");
        title.setTextSize(24f);
        title.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        mainLayout.addView(title);

        // Device ID display
        deviceIdText = new TextView(this);
        deviceIdText.setText("Device ID: " + (deviceId == null ? "(none)" : deviceId));
        deviceIdText.setTextSize(14f);
        deviceIdText.setPadding(0, 32, 0, 32);
        deviceIdText.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        mainLayout.addView(deviceIdText);

        // Scan QR Code Button
        scanQRButton = new Button(this);
        scanQRButton.setText("Scan Event QR Code");
        scanQRButton.setTextSize(18f);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 16, 0, 16);
        scanQRButton.setLayoutParams(buttonParams);
        scanQRButton.setOnClickListener(v -> openQRScanner());
        mainLayout.addView(scanQRButton);

        // View All Events Button
        viewEventsButton = new Button(this);
        viewEventsButton.setText("View All Events");
        viewEventsButton.setTextSize(18f);
        viewEventsButton.setLayoutParams(buttonParams);
        viewEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantActivity.class);
            startActivity(intent);
        });
        mainLayout.addView(viewEventsButton);

        // Profile Button (currently commented out, as in original)
//        profileButton = new Button(this);
//        profileButton.setText("My Profile");
//        profileButton.setTextSize(18f);
//        profileButton.setLayoutParams(buttonParams);
//        profileButton.setOnClickListener(v -> openProfile());
//        mainLayout.addView(profileButton);

        setContentView(mainLayout);
    }

    /**
     * Open the QR code scanner
     */
    private void openQRScanner() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivity(intent);
    }

    /**
     * Open the profile screen
     */
    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
}

package com.example.konoha_events;

import android.content.Intent;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.konoha_events.auth.EntrantDeviceIdStore;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * EntrantHomeActivity
 * -----------------------------------
 * home screen for entrant users identified by device ID.
 * Provides navigation to view guidelines and invitations.
 * Provides access to QR code scanner and other entrant features
 * Implements US 01.07.01 (Device login)
 */
public class EntrantHomeActivity extends AppCompatActivity {

    private Button scanQRButton, viewEventsButton, profileButton;
    private TextView deviceIdText;


    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_home);

        // Retrieve or create the device ID
        String deviceId = EntrantDeviceIdStore.getOrCreateId(this);

        // Basic UI
        TextView tv = new TextView(this);
        tv.setText("Entrant Home\n\nDevice ID:\n"+ (deviceId == null ? "(none yet)" : deviceId));
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

        // View All Events Button (placeholder for future)
        viewEventsButton = new Button(this);
        viewEventsButton.setText("View All Events");
        viewEventsButton.setTextSize(18f);
        viewEventsButton.setLayoutParams(buttonParams);
        viewEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrantActivity.class);
            startActivity(intent);
        });
        mainLayout.addView(viewEventsButton);

        // Profile Button
//        profileButton = new Button(this);
//        profileButton.setText("My Profile");
//        profileButton.setTextSize(18f);
//        profileButton.setLayoutParams(buttonParams);
//        profileButton.setOnClickListener(v -> openProfile());
//        mainLayout.addView(profileButton);

        setContentView(mainLayout);

        // Initialize Firebase and Location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        // Fetch and upload location directly — assumes permission is already granted
        fetchAndUploadLocation(deviceId);
    }

    /**
     * Open the QR code scanner
     */
    private void openQRScanner() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivity(intent);
    }

    private void fetchAndUploadLocation(String deviceId) {
        // Skip runtime permission request completely
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Do nothing (or optionally toast)
            Toast.makeText(this, "Location permission not granted in settings.", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // Toast for debugging
                            Toast.makeText(EntrantHomeActivity.this,
                                    "Location captured: " + latitude + ", " + longitude,
                                    Toast.LENGTH_SHORT).show();

                            // Upload to Firestore
                            Map<String, Object> data = new HashMap<>();
                            data.put("deviceId", deviceId);
                            data.put("latitude", latitude);
                            data.put("longitude", longitude);
                            data.put("timestamp", System.currentTimeMillis());

                            db.collection("entrants")
                                    .document(deviceId)
                                    .set(data)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(EntrantHomeActivity.this,
                                                "Location saved to Firebase.",
                                                Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(EntrantHomeActivity.this,
                                                "Failed to save location: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            Toast.makeText(EntrantHomeActivity.this,
                                    "Could not fetch location (null).",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Open the profile screen
     */
    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
}
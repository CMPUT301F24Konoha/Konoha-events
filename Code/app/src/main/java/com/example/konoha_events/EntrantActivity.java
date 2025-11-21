package com.example.konoha_events;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import models.EventModel;
import services.FirebaseService;

/**
 * EntrantActivity
 * -----------------------
 * Main screen for entrant users:
 * - Shows list of events they can interact with
 * - Allows joining waiting lists
 * - Navigates to profile, "My Events", and QR scanner
 * - On entry, attempts to capture the entrant's location and store it in Firestore
 *   so organizers can later visualize where entrants joined from.
 */
public class EntrantActivity extends AppCompatActivity {

    private RecyclerView recyclerEvents;
    private EventsAdapter eventsAdapter;
    private Button scanQRCodeButton;

    // For geolocation feature
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_all_events);

        // Initialize Firebase and location client for geolocation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        // Get the currently logged in user's ID from FirebaseService
        String userId = FirebaseService.firebaseService.getCurrentUserId();
        if (userId != null) {
            // After successful entry as an entrant, attempt to capture and upload location
            fetchAndUploadLocation(userId);
        }

        recyclerEvents = findViewById(R.id.recyclerEvents);
        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));

        eventsAdapter = new EventsAdapter(new EventsAdapter.Callback() {
            @Override
            public void onRowClick(EventModel event) {
                new androidx.appcompat.app.AlertDialog.Builder(EntrantActivity.this)
                        .setTitle("Join waiting list?")
                        .setPositiveButton("Join", (d, w) -> {
                            String userId = FirebaseService.firebaseService.getCurrentUserId();
                            FirebaseService.firebaseService.joinWaitingList(event.getId(), userId);
                            Toast.makeText(EntrantActivity.this, "Joined waiting list!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onQrClick(EventModel event) {
                // TODO: Open QR code
            }
        });

        recyclerEvents.setAdapter(eventsAdapter);

        FirebaseService.firebaseService.getEventsLiveData().observe(
                this,
                new Observer<ArrayList<EventModel>>() {
                    @Override
                    public void onChanged(ArrayList<EventModel> eventModels) {
                        eventsAdapter.submitList(eventModels);
                    }
                }
        );

        ImageButton profileButton = findViewById(R.id.profile);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        Button my_events_button = findViewById(R.id.My_Events_Button);
        my_events_button.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantActivity.this, EntrantMyEventActivity.class);
            startActivity(intent);
        });

        scanQRCodeButton = findViewById(R.id.scan_qr_code_button);
        scanQRCodeButton.setOnClickListener(v -> openQRScanner());
    }

    private void openQRScanner() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivity(intent);
    }

    /**
     * Attempts to fetch the device's last known location and upload it to Firestore
     * under the current entrant's ID. This method assumes the user has already
     * granted location permission in system settings; it does not request permission
     * at runtime.
     *
     * @param userId the Firestore user ID associated with this entrant
     */
    private void fetchAndUploadLocation(String userId) {
        // Do NOT prompt the user for permission; only proceed if already granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Location permission not granted – skip silently or show a light toast
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

                            // Optional debug toast
                            Toast.makeText(EntrantActivity.this,
                                    "Location captured: " + latitude + ", " + longitude,
                                    Toast.LENGTH_SHORT).show();

                            // Prepare data to upload
                            Map<String, Object> data = new HashMap<>();
                            data.put("userId", userId);
                            data.put("latitude", latitude);
                            data.put("longitude", longitude);
                            data.put("timestamp", System.currentTimeMillis());

                            // Store under 'entrants/{userId}' collection
                            db.collection("entrants")
                                    .document(userId)
                                    .set(data)
                                    .addOnSuccessListener(aVoid -> {
                                        // Optional success toast
                                        Toast.makeText(EntrantActivity.this,
                                                "Location saved to Firebase.",
                                                Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(EntrantActivity.this,
                                                "Failed to save location: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            Toast.makeText(EntrantActivity.this,
                                    "Could not fetch location (null).",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

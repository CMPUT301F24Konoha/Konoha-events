package com.example.konoha_events;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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
 *
 * Implements US 01.07.01 (Device login)
 */
public class EntrantHomeActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_home);

        // Retrieve or create the device ID
        String id = EntrantDeviceIdStore.getOrCreateId(this);

        // Basic UI
        TextView tv = new TextView(this);
        tv.setText("Entrant Home\n\nDevice ID:\n" + (id == null ? "(none yet)" : id));
        tv.setTextSize(18f);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad, pad, pad);
        setContentView(tv);

        // Initialize Firebase and Location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        // Fetch and upload location directly — assumes permission is already granted
        fetchAndUploadLocation(id);
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
}

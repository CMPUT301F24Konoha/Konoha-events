package com.example.konoha_events;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

import constants.DatabaseConstants;
import constants.IntentConstants;
import services.FirebaseService;

/**
 * LoginActivity
 * -------------
 * Handles role-based login for administrators, organizers, and entrants.
 * <p>
 * Features:
 * <ul>
 *     <li>Username/password login for all roles.</li>
 *     <li>Automatic device-based login for entrants (US 01.07.01).</li>
 *     <li>When an entrant logs in with username/password, their deviceId is
 *         associated with their user account so future device-based logins work.</li>
 *     <li>Captures precise location of entrant upon login, stores the location with
 *          with userid along with latitude and longitude</li>
 * </ul>
 */

public class LoginActivity extends AppCompatActivity {
    private FirebaseService fbs;
    private EditText usernameInput, passwordInput;
    private Button signInButton;
    private TextView signUpLink, backToRole, loginSubtitle;
    private DatabaseConstants.USER_TYPE selectedRole;

    // Location + Firestore for entrant geolocation
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fbs = FirebaseService.firebaseService;

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        signInButton = findViewById(R.id.signInButton);
        signUpLink = findViewById(R.id.signUpLink);
        backToRole = findViewById(R.id.backToRole);
        loginSubtitle = findViewById(R.id.loginSubtitle);

        // Init location + Firestore
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        // The role (ADMINISTRATOR / ORGANIZER / ENTRANT) is passed from HomeActivity.
        selectedRole = (DatabaseConstants.USER_TYPE) getIntent()
                .getSerializableExtra(IntentConstants.INTENT_ROLE_NAME);

        // US 01.07.01 - Device-based identification for entrants
        // ------------------------------------------------------
        // If the user selected "Entrant" on the previous screen, we try to log them in
        // using only their deviceId, without asking for username/password.
        //
        // If a user with this deviceId exists in Firestore, they are logged in and
        // sent directly to EntrantActivity. Otherwise, the normal login UI is used.
        if (selectedRole == DatabaseConstants.USER_TYPE.ENTRANT) {
            String deviceId = EntrantDeviceIdStore.getOrCreateId(this);

            fbs.loginWithDeviceId(deviceId, succeeded -> {
                if (succeeded) {
                    // Device matches an existing entrant → go straight to entrant dashboard.
                    fetchAndUploadLocation(deviceId);
                    Intent i = new Intent(LoginActivity.this, EntrantActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    // No matching user found for this deviceId → user must log in manually.
                }
            });
        }

        // Update subtitle dynamically to show which role is being logged in
        if (selectedRole != null) {
            loginSubtitle.setText("Sign in as " + selectedRole);
        }
        // Handle explicit username/password login for all roles
        signInButton.setOnClickListener(v -> {

            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            fbs.login(username, password, userType -> {
                switch (userType) {
                    case ADMINISTRATOR:
                        Intent intent = new Intent(this, AdminActivity.class);
                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "Successfully logged in as administrator", Toast.LENGTH_SHORT).show();
                        return;

                    case ORGANIZER:
                        Intent orgIntent = new Intent(this, OrganizerActivity.class);
                        startActivity(orgIntent);
                        Toast.makeText(LoginActivity.this, "Successfully logged in as organizer", Toast.LENGTH_SHORT).show();
                        return;

                    case ENTRANT:
                        // When an entrant logs in with username/password, we bind their deviceId
                        // to their Firestore user document so future device-based login works.
                        Intent entrant = new Intent(this, EntrantActivity.class);
                        startActivity(entrant);
                        Toast.makeText(LoginActivity.this, "Successfully logged in as entrant", Toast.LENGTH_SHORT).show();
                        String deviceId = EntrantDeviceIdStore.getOrCreateId(LoginActivity.this);
                        fbs.updateDeviceIdForCurrentUser(deviceId);
                        fetchAndUploadLocation(deviceId);
                        return;

                    case NULL:
                        Toast.makeText(LoginActivity.this, "Login was unsuccessful", Toast.LENGTH_SHORT).show();
                        return;

                    default:
                        Toast.makeText(
                                LoginActivity.this,
                                String.format("Unexpected enum value received %s", userType),
                                Toast.LENGTH_SHORT
                        ).show();
                }
            });
        });

        backToRole.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });

        signUpLink.setOnClickListener(v ->
                Toast.makeText(this, "Sign-up screen coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Fetches the device's last known location and uploads it to Firestore
     * in the "entrants" collection, keyed by deviceId.
     * Shows toasts:
     *  - "Location captured: lat, lng"
     *  - "Location saved to Firebase." / error message
     */
    private void fetchAndUploadLocation(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return;
        }

        // Permission check only – no runtime request (same behaviour as old EntrantHomeActivity)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
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

                            // Toast for debugging – this is your old "Location captured" message
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Location captured: " + latitude + ", " + longitude,
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Upload to Firestore
                            Map<String, Object> data = new HashMap<>();
                            data.put("deviceId", deviceId);
                            data.put("latitude", latitude);
                            data.put("longitude", longitude);
                            data.put("timestamp", System.currentTimeMillis());

                            db.collection("entrants")
                                    .document(deviceId)
                                    .set(data)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(
                                            LoginActivity.this,
                                            "Location saved to Firebase.",
                                            Toast.LENGTH_SHORT
                                    ).show())
                                    .addOnFailureListener(e -> Toast.makeText(
                                            LoginActivity.this,
                                            "Failed to save location: " + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show());
                        } else {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Could not fetch location (null).",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
    }
}

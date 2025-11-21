package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import constants.DatabaseConstants;
import constants.IntentConstants;
import services.FirebaseService;

import com.example.konoha_events.auth.EntrantDeviceIdStore;

public class LoginActivity extends AppCompatActivity {
    private FirebaseService fbs;
    private EditText usernameInput, passwordInput;
    private Button signInButton;
    private TextView signUpLink, backToRole, loginSubtitle;
    private DatabaseConstants.USER_TYPE selectedRole;

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

        selectedRole = (DatabaseConstants.USER_TYPE) getIntent()
                .getSerializableExtra(IntentConstants.INTENT_ROLE_NAME);

        if (selectedRole == DatabaseConstants.USER_TYPE.ENTRANT) {
            String deviceId = EntrantDeviceIdStore.getOrCreateId(this);


            // Try to find user with this deviceId in Firebase
            fbs.loginWithDeviceId(deviceId, succeeded -> {
                if (succeeded) {
                    // Device matches an existing user, go straight to EntrantActivity
                    Intent i = new Intent(LoginActivity.this, EntrantActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    // Device not known yet → just let the user log in normally
                    // (UI is already set up below)
                }
            });
        }

        // Update subtitle dynamically
        if (selectedRole != null) {
            loginSubtitle.setText("Sign in as " + selectedRole);
        }

        signInButton.setOnClickListener(v -> {

            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            fbs.login(username, password, userType -> {
                switch(userType) {
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
                        Intent entrant = new Intent(this, EntrantActivity.class);
                        startActivity(entrant);
                        Toast.makeText(LoginActivity.this, "Successfully logged in as entrant", Toast.LENGTH_SHORT).show();
                        String deviceId = EntrantDeviceIdStore.getOrCreateId(LoginActivity.this);
                        fbs.updateDeviceIdForCurrentUser(deviceId);
                        return;
                    case NULL:
                        Toast.makeText(LoginActivity.this, "Login was unsuccessful", Toast.LENGTH_SHORT).show();
                        return;
                    default:
                        Toast.makeText(LoginActivity.this,
                                String.format("Unexpected enum value received %s", userType), Toast.LENGTH_SHORT).show();
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
}

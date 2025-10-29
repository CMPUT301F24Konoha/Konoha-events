package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button signInButton;
    private TextView signUpLink, backToRole, loginSubtitle;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        signInButton = findViewById(R.id.signInButton);
        signUpLink = findViewById(R.id.signUpLink);
        backToRole = findViewById(R.id.backToRole);
        loginSubtitle = findViewById(R.id.loginSubtitle);

        selectedRole = getIntent().getStringExtra("role");

        // Update subtitle dynamically
        if (selectedRole != null) {
            loginSubtitle.setText("Sign in as " + selectedRole);
        }

        signInButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Placeholder logic for Admin (bypasses validation)
            if ("Admin".equals(selectedRole)) {
                // TODO: Replace with actual admin authentication in future
                Intent intent = new Intent(this, AdminActivity.class);
                startActivity(intent);
                Toast.makeText(this, "Admin login bypass (temporary)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Placeholder Organizer/Entrant logic
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, selectedRole + " login placeholder", Toast.LENGTH_SHORT).show();
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

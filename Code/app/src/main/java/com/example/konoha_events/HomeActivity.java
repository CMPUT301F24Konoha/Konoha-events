package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * HomeActivity
 * ----------------------
 * The first screen of the app where users select their role.
 * For now:
 *  - Admin → navigates to AdminDashboardActivity.
 *  - Entrant and Organizer → show placeholder toasts.
 *
 * Other role dashboards are commented out and will be implemented later.
 */

public class HomeActivity extends AppCompatActivity {

    private Button entrantButton, organizerButton, adminButton, continueButton;
    private String selectedRole = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        entrantButton = findViewById(R.id.entrantButton);
        organizerButton = findViewById(R.id.organizerButton);
        adminButton = findViewById(R.id.adminButton);
        continueButton = findViewById(R.id.continueButton);

        View.OnClickListener roleClickListener = view -> {
            entrantButton.setAlpha(1f);
            organizerButton.setAlpha(1f);
            adminButton.setAlpha(1f);

            if (view == entrantButton) selectedRole = "Entrant";
            else if (view == organizerButton) selectedRole = "Organizer";
            else if (view == adminButton) selectedRole = "Admin";

            view.setAlpha(0.7f);
            continueButton.setEnabled(true);
        };

        entrantButton.setOnClickListener(roleClickListener);
        organizerButton.setOnClickListener(roleClickListener);
        adminButton.setOnClickListener(roleClickListener);


        continueButton.setOnClickListener(v -> {
            if (selectedRole == null) {
                Toast.makeText(this, "Please select a role first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent;

            switch (selectedRole) {
                case "Admin":
                case "Organizer":
                    //  Navigate to LoginActivity
                    intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("role", selectedRole);
                    startActivity(intent);
                    break;

                case "Entrant":
                    // 🟡 Placeholder for Entrant flow
                    Toast.makeText(this, "Entrant dashboard navigation goes here", Toast.LENGTH_SHORT).show();
                    // TODO: startActivity(new Intent(this, EntrantDashboardActivity.class));
                    break;

                default:
                    Toast.makeText(this, "Unexpected role: " + selectedRole, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}

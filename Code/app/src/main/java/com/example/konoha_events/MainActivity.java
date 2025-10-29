package com.example.konoha_events;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.konoha_events.databinding.ActivityMainBinding;

/**
 * MainActivity
 * ----------------------
 * Clean version of the MainActivity after removing navigation fragments.
 * This serves as a placeholder or entry point activity.
 *
 * You can later repurpose it for app initialization or splash logic.
 */

public class MainActivity extends AppCompatActivity {

    // View binding for layout access
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Optional: set toolbar if you're still using one
        // setSupportActionBar(binding.toolbar);

        // Simple placeholder message to confirm it's working
        if (binding.placeholderText != null) {
            binding.placeholderText.setText("Main Activity Loaded Successfully");
        }
    }
}

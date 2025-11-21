package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konoha_events.ProfileActivity;

import java.util.ArrayList;

import models.EventModel;
import services.FirebaseService;

public class EntrantActivity extends AppCompatActivity {
    private RecyclerView recyclerEvents;
    private EventsAdapter eventsAdapter;
    private Button scanQRCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_all_events);

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
                //TODO: Open QR code
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

        ImageButton messagesButton = findViewById(R.id.imageButton2);
        messagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantActivity.this, EntrantInvitationsActivity.class);
            startActivity(intent);
        });
    }

    private void openQRScanner() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivity(intent);
    }
}

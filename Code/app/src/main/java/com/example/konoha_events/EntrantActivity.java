package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.Date;
import models.OnWaitingListModel;

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
                String userId = FirebaseService.firebaseService.getCurrentUserId();

                OnWaitingListModel existing = FirebaseService.firebaseService
                        .getExistingWaitlistEntry(event.getId(), userId);
                //Check if the user has already joined the waiting list, has accepted
                //declined or is selected for the event.
                if (existing != null && existing.getStatus() != null) {

                    String status = existing.getStatus().name();

                    new androidx.appcompat.app.AlertDialog.Builder(EntrantActivity.this)
                            .setTitle("Hold on!")
                            .setMessage("You are already " + status + " for this event.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                Date deadline = event.getRegistrationDeadline();
                Date now = new Date();
                //Check if the registration deadline has already passed
                if (deadline != null && deadline.before(now)) {
                    new androidx.appcompat.app.AlertDialog.Builder(EntrantActivity.this)
                            .setTitle("Registration Closed")
                            .setMessage("The registration deadline for this event has already passed.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
                //alert user to join waiting list if the above conditions are false.

                new androidx.appcompat.app.AlertDialog.Builder(EntrantActivity.this)
                        .setTitle("Join waiting list?")
                        .setPositiveButton("Join", (d, w) -> {
                            FirebaseService.firebaseService.joinWaitingList(event.getId(), userId);
                            Toast.makeText(
                                    EntrantActivity.this,
                                    "Joined waiting list!",
                                    Toast.LENGTH_SHORT
                            ).show();
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
            overridePendingTransition(0, 0);
        });

        Button History_Button = findViewById(R.id.History_Button);
        History_Button.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantActivity.this, EntrantHistory.class);
            startActivity(intent);
        });

        Button Filter = findViewById(R.id.Filter_Button);
        Filter.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantActivity.this, EntrantFilterEvents.class);
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

package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import constants.DatabaseConstants;
import models.EventModel;
import models.OnWaitingListModel;
import services.FirebaseService;

public class EntrantMyEventActivity extends AppCompatActivity {

    private RecyclerView recyclerMyEvents;
    private EventsAdapter adapter;
    private ArrayList<EventModel> allEvents = new ArrayList<>();
    private ArrayList<OnWaitingListModel> waitlistRows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_my_events);

        ImageButton profile = findViewById(R.id.profile);
        if (profile != null) {
            profile.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class))
            );
        }
        Button allEventsBtn = findViewById(R.id.All_Events_Button);
        allEventsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantMyEventActivity.this, EntrantActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);

        });

        Button History_Button = findViewById(R.id.History_Button);
        History_Button.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantMyEventActivity.this, EntrantHistory.class);
            startActivity(intent);
        });

        ImageButton messagesButton = findViewById(R.id.imageButton2);
        messagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantMyEventActivity.this, EntrantInvitationsActivity.class);
            startActivity(intent);
        });

        recyclerMyEvents = findViewById(R.id.recyclerMyEvents);
        recyclerMyEvents.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EventsAdapter(new EventsAdapter.Callback() {
            @Override
            public void onRowClick(EventModel event) {
                new androidx.appcompat.app.AlertDialog.Builder(EntrantMyEventActivity.this)
                        .setTitle("Leave waitlist?")
                        .setPositiveButton("Leave", (d, w) -> {
                            String userId = FirebaseService.firebaseService.getCurrentUserId();
                            FirebaseService.firebaseService.leaveWaitingList(event.getId(), userId);
                            Toast.makeText(EntrantMyEventActivity.this, "Removed from waitlist.", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onQrClick(EventModel event) {
                //TODO: Show QR code to entrant.
            }
        });
        //Change button title instead of making a whole new view for it. If this doesn't work in the future,
        //I can make completely seperate views.
        adapter.setPrimaryButtonLabel("Leave Waitlist");
        recyclerMyEvents.setAdapter(adapter);

        FirebaseService.firebaseService.getEventsLiveData().observe(this, list -> {
            allEvents = (list == null) ? new ArrayList<>() : list;
            recomputeAndShow();
        });

        FirebaseService.firebaseService.getOnWaitingListLiveData().observe(this, list -> {
            waitlistRows = (list == null) ? new ArrayList<>() : list;
            recomputeAndShow();
        });
    }

    //Helper to find all the events a user is signed up for.
    //Collect eventIds where the user is on the waitlist for.
    private void recomputeAndShow() {
        String userId = FirebaseService.firebaseService.getCurrentUserId();
        Set<String> myEventIds = new HashSet<>();
        for (OnWaitingListModel row : waitlistRows) {
            if (row == null) continue;
            String uid = row.getUserId();
            String eid = row.getEventId();
            DatabaseConstants.ON_WAITING_LIST_STATUS status = row.getStatus();

            if (uid == null || eid == null || status == null) continue;
            //I think this is all of them that a user would be apart of
            //TODO: Add any extra waiting list status
            //Find all onWaitingList entries that the user is on, add the events to the Hashset.
            if (uid.equals(userId)
                    && (status == DatabaseConstants.ON_WAITING_LIST_STATUS.WAITING
                    || status == DatabaseConstants.ON_WAITING_LIST_STATUS.ACCEPTED
                    || status == DatabaseConstants.ON_WAITING_LIST_STATUS.SELECTED)) {
                myEventIds.add(eid);
            }
        }

        ArrayList<EventModel> mine = new ArrayList<>();
        for (EventModel e : allEvents) {
            if (e != null && myEventIds.contains(e.getId())) {
                mine.add(e);
            }
        }

        adapter.submitList(mine);
    }
}

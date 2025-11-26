package com.example.konoha_events;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import constants.DatabaseConstants;
import models.EventModel;
import models.OnWaitingListModel;
import services.FirebaseService;

public class EntrantHistory extends AppCompatActivity {

    private ArrayList<EventModel> allEvents = new ArrayList<>();
    private ArrayList<OnWaitingListModel> waitlistRows = new ArrayList<>();
    private EntrantHistoryAdapter adapter;
    private RecyclerView recyclerHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_history);

        // Back button
        ImageButton back = findViewById(R.id.back_button);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        recyclerHistory = findViewById(R.id.recyclerHistory);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EntrantHistoryAdapter();
        recyclerHistory.setAdapter(adapter);

        FirebaseService.firebaseService.getEventsLiveData().observe(this, list -> {
            allEvents = (list == null) ? new ArrayList<>() : list;
            recomputeAndShow();
        });
        FirebaseService.firebaseService.getOnWaitingListLiveData().observe(this, list -> {
            waitlistRows = (list == null) ? new ArrayList<>() : list;
            recomputeAndShow();
        });
    }

    /**
     * Show all events for this user where status is
     * WAITING, ACCEPTED, SELECTED, DECLINED, or CANCELLED.
     */
    private void recomputeAndShow() {
        String userId = FirebaseService.firebaseService.getCurrentUserId();
        if (userId == null) return;

        Map<String, DatabaseConstants.ON_WAITING_LIST_STATUS> statusByEventId = new HashMap<>();

        for (OnWaitingListModel row : waitlistRows) {
            if (row == null) continue;
            String uid = row.getUserId();
            String eid = row.getEventId();
            DatabaseConstants.ON_WAITING_LIST_STATUS status = row.getStatus();
            if (uid == null || eid == null || status == null) continue;
            if (!uid.equals(userId)) continue;

            if (status == DatabaseConstants.ON_WAITING_LIST_STATUS.WAITING
                    || status == DatabaseConstants.ON_WAITING_LIST_STATUS.ACCEPTED
                    || status == DatabaseConstants.ON_WAITING_LIST_STATUS.SELECTED
                    || status == DatabaseConstants.ON_WAITING_LIST_STATUS.DECLINED
                    || status == DatabaseConstants.ON_WAITING_LIST_STATUS.CANCELLED) {
                statusByEventId.put(eid, status);
            }
        }

        //Use history adapter to create a list of events the user is signed up for, sort as event and status
        ArrayList<EntrantHistoryAdapter.HistoryItem> historyItems = new ArrayList<>();
        for (EventModel e : allEvents) {
            if (e == null) continue;
            DatabaseConstants.ON_WAITING_LIST_STATUS st = statusByEventId.get(e.getId());
            if (st != null) {
                historyItems.add(new EntrantHistoryAdapter.HistoryItem(e, st));
            }
        }

        adapter.submitList(historyItems);
    }
}

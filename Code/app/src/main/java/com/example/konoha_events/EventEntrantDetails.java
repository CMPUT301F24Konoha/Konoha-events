package com.example.konoha_events;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

import constants.DatabaseConstants;
import constants.IntentConstants;
import interfaces.OnWaitingListArrayListCallback;
import interfaces.UserModelArrayListCallback;
import models.OnWaitingListModel;
import models.UserModel;
import services.FirebaseService;
import util.ViewUtil;
import views.UserAdminDashboardView;

public class EventEntrantDetails extends AppCompatActivity {
    private final String tag = "[EventEntrantDetails]";
    private FirebaseService fbs;
    private ListView entrantDetailsListView;
    private MaterialButtonToggleGroup materialButtonToggleGroup;
    private ArrayList<UserModel> entrantUserModels;
    private UserAdminDashboardView userAdminDashboardView;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_entrants_view);

        fbs = FirebaseService.firebaseService;

        Toolbar toolbar = findViewById(R.id.activity_event_entrants_toolbar);

        String returnActivityName = getIntent().getStringExtra(IntentConstants.INTENT_VIEW_EVENT_CALLER_TYPE);
        eventId = getIntent().getStringExtra(IntentConstants.INTENT_VIEW_EVENT_EVENT_ID);

        Intent intent = new Intent(this, EventDetails.class);
        intent.putExtra(IntentConstants.INTENT_VIEW_EVENT_EVENT_ID, eventId);
        intent.putExtra(IntentConstants.INTENT_VIEW_EVENT_CALLER_TYPE, returnActivityName);
        ViewUtil.setupToolbarWithIntent(this, toolbar, "Entrant Details", intent);

        entrantDetailsListView = findViewById(R.id.activity_event_entrant_list_view);
        materialButtonToggleGroup = findViewById(R.id.activity_event_entrants_button_toggle_group);

        entrantUserModels = new ArrayList<UserModel>();
        userAdminDashboardView = new UserAdminDashboardView(this, entrantUserModels);
        entrantDetailsListView.setAdapter(userAdminDashboardView);

        materialButtonToggleGroup.addOnButtonCheckedListener((view, id, isChecked) -> {
            if (!isChecked) return;

            if (id == R.id.activity_event_entrants_wait_button) {
                displayEntrantsOfStatus(DatabaseConstants.ON_WAITING_LIST_STATUS.WAITING);
            } else if (id == R.id.activity_event_entrants_select_button) {
                displayEntrantsOfStatus(DatabaseConstants.ON_WAITING_LIST_STATUS.SELECTED);
            } else if (id == R.id.activity_event_entrants_accept_button) {
                displayEntrantsOfStatus(DatabaseConstants.ON_WAITING_LIST_STATUS.ACCEPTED);
            } else if (id == R.id.activity_event_entrants_decline_button) {
                displayEntrantsOfStatus(DatabaseConstants.ON_WAITING_LIST_STATUS.DECLINED);
            } else if (id == R.id.activity_event_entrants_cancel_button) {
                displayEntrantsOfStatus(DatabaseConstants.ON_WAITING_LIST_STATUS.CANCELLED);
            }
        });
    }

    private void displayEntrantsOfStatus(DatabaseConstants.ON_WAITING_LIST_STATUS status) {
        fbs.getUsersOfEventWithStatus(eventId, status, new UserModelArrayListCallback() {
            @Override
            public void onCompleted(ArrayList<UserModel> userModels) {
                Log.i(tag, "Received details: " + userModels.toString());
                entrantUserModels.clear();
                entrantUserModels.addAll(userModels);
                userAdminDashboardView.notifyDataSetChanged();
            }
        });
    }
}

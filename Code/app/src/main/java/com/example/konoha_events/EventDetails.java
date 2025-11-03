package com.example.konoha_events;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

import constants.IntentConstants;
import interfaces.OnWaitingListArrayListCallback;
import models.EventModel;
import models.OnWaitingListModel;
import services.FirebaseService;
import util.ModelUtil;
import util.ViewUtil;

public class EventDetails extends AppCompatActivity {
    private final String tag = "[EventDetails]";
    private FirebaseService fbs;
    private TextView totalOnListTextView;
    private TextView waitingTextView;
    private TextView selectedTextView;
    private TextView acceptedTextView;
    private TextView declinedTextView;
    private TextView cancelledTextView;
    private Button viewEntrantsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        fbs = FirebaseService.firebaseService;

        Toolbar toolbar = findViewById(R.id.activity_event_toolbar);

        String returnActivityName = getIntent().getStringExtra(IntentConstants.INTENT_VIEW_EVENT_CALLER_TYPE);
        String eventId = getIntent().getStringExtra(IntentConstants.INTENT_VIEW_EVENT_EVENT_ID);
        Class<?> returnActivityClass = null;
        if (returnActivityName == null) {
            Log.e(tag, "EventDetails activity started with null returnActivityName intent");
            return;
        } else if (eventId == null) {
            Log.e(tag, "EventDetails activity started with null eventId intent");
            return;
        }

        try {
            returnActivityClass = Class.forName(returnActivityName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        totalOnListTextView = findViewById(R.id.activity_event_view_total_on_list_text);
        waitingTextView = findViewById(R.id.activity_event_view_waiting_text);
        selectedTextView = findViewById(R.id.activity_event_view_selected_text);
        acceptedTextView = findViewById(R.id.activity_event_view_accepted_text);
        declinedTextView = findViewById(R.id.activity_event_view_declined_text);
        cancelledTextView = findViewById(R.id.activity_event_view_cancelled_text);
        viewEntrantsButton = findViewById(R.id.activity_event_view_entrants_button);

        DocumentReference eventDocument = fbs.getEventDocumentReference(eventId);
        Class<?> finalReturnActivityClass = returnActivityClass;
        eventDocument
                .get()
                .addOnSuccessListener((v) -> {
                    Log.i(tag, String.format("Got event model of event %s successfully", eventId));
                    EventModel eventModel = ModelUtil.toEventModel(v);
                    ViewUtil.setupToolbarWithBackButtonToActivity(this, toolbar, eventModel.getEventTitle(), (Class<? extends Activity>) finalReturnActivityClass);
                })
                .addOnFailureListener((e) -> Log.i(tag,
                        String.format("Didn't find or doesn't exist event %s", eventId)));

        fbs.getOnWaitingListsOfEvent(eventId, new OnWaitingListArrayListCallback() {
            @Override
            public void onCompleted(ArrayList<OnWaitingListModel> onWaitingListModels) {
                Log.i(tag, "Successfully got on waiting list models for event ");
                int waitingCount = 0;
                int selectedCount = 0;
                int acceptedCount = 0;
                int declinedCount = 0;
                int cancelledCount = 0;
                int total = onWaitingListModels.size();
                for (OnWaitingListModel owm : onWaitingListModels) {
                    switch (owm.getStatus()) {
                        case WAITING:
                            waitingCount++;
                            break;
                        case SELECTED:
                            selectedCount++;
                            break;
                        case ACCEPTED:
                            acceptedCount++;
                            break;
                        case DECLINED:
                            declinedCount++;
                            break;
                        case CANCELLED:
                            cancelledCount++;
                            break;
                        default:
                            Log.e(tag, "Unknown status in on waiting list model: " + owm.getStatus());
                            break;
                    }
                }

                displayTotals(total);
                displayWaiting(waitingCount);
                displaySelected(selectedCount);
                displayAccepted(acceptedCount);
                displayDeclined(declinedCount);
                displayCancelled(cancelledCount);
            }
        });

        viewEntrantsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventEntrantDetails.class);
            intent.putExtra(IntentConstants.INTENT_VIEW_EVENT_EVENT_ID, eventId);
            intent.putExtra(IntentConstants.INTENT_VIEW_EVENT_CALLER_TYPE, returnActivityName);
            startActivity(intent);
        });
    }

    private void displayTotals(int total) {
        totalOnListTextView.setText(
                String.format("Total: %s", total));
    }

    private void displayWaiting(int waiting) {
        waitingTextView.setText(
                String.format("Waiting: %s", waiting));
    }

    private void displaySelected(int selected) {
        selectedTextView.setText(
                String.format("Selected: %s", selected));
    }

    private void displayAccepted(int accepted) {
        acceptedTextView.setText(
                String.format("Accepted: %s", accepted));
    }

    private void displayDeclined(int declined) {
        declinedTextView.setText(
                String.format("Declined: %s", declined));
    }

    private void displayCancelled(int cancelled) {
        cancelledTextView.setText(
                String.format("Cancelled: %s", cancelled));
    }
}

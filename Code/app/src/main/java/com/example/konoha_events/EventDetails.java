package com.example.konoha_events;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.DocumentReference;

import constants.IntentConstants;
import models.EventModel;
import services.FirebaseService;
import util.ViewUtil;

public class EventDetails extends AppCompatActivity {
    private final String tag = "[EventDetails]";
    private FirebaseService fbs;

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

        ViewUtil.setupToolbarWithBackButtonToActivity(this, toolbar, "Event", (Class<? extends Activity>) returnActivityClass);

        DocumentReference eventModel = fbs.getEventDocumentReference(eventId);
        eventModel
                .get()
                .addOnSuccessListener((v) -> {
                    Log.i(tag, String.format("Got event model of event %s successfully", eventId));
                    //Below to see that we can get the ridght event
                    Log.i(tag, v.toString());
                })
                .addOnFailureListener((e) -> Log.i(tag,
                        String.format("Didn't find or doesn't exist event %s", eventId)));
    }
}

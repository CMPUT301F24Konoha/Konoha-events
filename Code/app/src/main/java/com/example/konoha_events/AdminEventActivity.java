package com.example.konoha_events;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import models.EventModel;
import services.FirebaseService;
import util.ViewUtil;
import views.EventAdminDashboardView;
import views.UserAdminDashboardView;

public class AdminEventActivity extends AppCompatActivity {
    private final String tag = "[AdminEventActivity]";
    private FirebaseService fbs;
    private ListView listView;
    private ArrayList<EventModel> eventModelDataList;
    private EventAdminDashboardView eventAdminDashboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll);

        fbs = FirebaseService.firebaseService;

        Toolbar toolbar = findViewById(R.id.activity_scroll_toolbar);
        ViewUtil.setupToolbarWithBackButtonToParent(this, toolbar, "Manage Events");

        listView = findViewById(R.id.activity_scroll_listview);

        eventModelDataList = fbs.getEventsLiveData().getValue();
        eventAdminDashboardView = new EventAdminDashboardView(this, eventModelDataList, this.getClass());
        listView.setAdapter(eventAdminDashboardView);
    }
}

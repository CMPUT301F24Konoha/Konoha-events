package com.example.konoha_events;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import interfaces.HasImage;
import models.EventModel;
import models.UserModel;
import services.FirebaseService;
import util.ViewUtil;
import views.ImageAdminDashboardView;
import views.UserAdminDashboardView;

public class AdminImageActivity extends AppCompatActivity {
    private final String tag = "[AdminImageActivity]";
    private FirebaseService fbs;
    private ListView listView;
    private ArrayList<HasImage> imageModelDataList = new ArrayList<HasImage>();
    private ImageAdminDashboardView imageAdminDashboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll);

        fbs = FirebaseService.firebaseService;

        Toolbar toolbar = findViewById(R.id.activity_scroll_toolbar);
        ViewUtil.setupToolbarWithBackButtonToParent(this, toolbar, "Manage Images");

        listView = findViewById(R.id.activity_scroll_listview);

        ArrayList<EventModel> eventModels = fbs.getEventsLiveData().getValue();
        if (eventModels == null) {
            Log.e(tag, "Received null event models");
            return;
        }
        imageModelDataList.addAll(eventModels);

        imageAdminDashboardView = new ImageAdminDashboardView(this, imageModelDataList);
        listView.setAdapter(imageAdminDashboardView);
    }
}

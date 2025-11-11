package com.example.konoha_events;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import models.NotificationModel;
import services.FirebaseService;
import util.ViewUtil;
import views.NotificationAdminDashboardView;

public class AdminNotificationActivity extends AppCompatActivity {
    private final String tag = "[AdminNotificationActivity]";
    private FirebaseService fbs;
    private ListView listView;
    private ArrayList<NotificationModel> notificationModelsDataList = new ArrayList<>();
    private NotificationAdminDashboardView notificationAdminDashboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll);

        fbs = FirebaseService.firebaseService;

        Toolbar toolbar = findViewById(R.id.activity_scroll_toolbar);
        ViewUtil.setupToolbarWithBackButtonToParent(this, toolbar, "Manage Notifications");

        listView = findViewById(R.id.activity_scroll_listview);

        notificationModelsDataList = fbs.getNotificationsLiveData().getValue();
        notificationAdminDashboardView = new NotificationAdminDashboardView(this, notificationModelsDataList, this.getClass());
        listView.setAdapter(notificationAdminDashboardView);
    }
}

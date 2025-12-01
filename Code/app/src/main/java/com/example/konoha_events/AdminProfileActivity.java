package com.example.konoha_events;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import models.UserModel;
import services.FirebaseService;
import util.ViewUtil;
import views.UserAdminDashboardView;

/**
 * Activity for admins viewing the different profiles of the system.
 * Viewable only by logged in admins.
 */
public class AdminProfileActivity extends AppCompatActivity {
    private final String tag = "[AdminProfileActivity]";
    private FirebaseService fbs;
    private ListView listView;
    private ArrayList<UserModel> userModelDataList;
    private UserAdminDashboardView userAdminDashboardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll);

        fbs = FirebaseService.firebaseService;

        Toolbar toolbar = findViewById(R.id.activity_scroll_toolbar);
        ViewUtil.setupToolbarWithBackButtonToParent(this, toolbar, "Manage Profiles");

        listView = findViewById(R.id.activity_scroll_listview);

        userModelDataList = fbs.getUsersLiveData().getValue();
        userAdminDashboardView = new UserAdminDashboardView(this, userModelDataList, null);
        listView.setAdapter(userAdminDashboardView);
    }
}

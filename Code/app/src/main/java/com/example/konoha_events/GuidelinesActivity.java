package com.example.konoha_events;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class GuidelinesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guidelines);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lottery Guidelines");
        }
    }
}
package com.example.konoha_events;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.konoha_events.auth.EntrantDeviceIdStore;

public class EntrantHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        String id = EntrantDeviceIdStore.getOrCreateId(this);

        TextView tv = new TextView(this);
        tv.setText("Entrant Home\n\nDevice ID:\n"+ (id == null ? "(none yet)" : id));
        tv.setTextSize(18f);
        int pad = (int) (24 *getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad, pad, pad);

        setContentView(tv);
    }

}

package util;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ViewUtil {
    private static final String tag = "[ViewUtil]";
    public static void setupToolbarWithBackButtonToParent(AppCompatActivity activity,
                                                    androidx.appcompat.widget.Toolbar toolbar,
                                                    String displayName) {
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar == null) {
            Log.e(tag, "Failed to setup action bar. Could not find action bar.");
            return;
        }
        actionBar.setTitle(displayName);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    public static void setupToolbarWithBackButtonToActivity(AppCompatActivity activity,
                                                          androidx.appcompat.widget.Toolbar toolbar,
                                                          String displayName,
                                                          Class<? extends Activity> returnToParent) {
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar == null) {
            Log.e(tag, "Failed to setup action bar. Could not find action bar.");
            return;
        }
        actionBar.setTitle(displayName);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(activity, returnToParent);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            activity.finish();
        });
    }
}

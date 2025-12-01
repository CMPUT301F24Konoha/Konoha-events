package util;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Contains various helpers for creating UI components or views that require
 * the same/similar implementations.
 */
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

    public static void setupToolbarWithIntent(AppCompatActivity activity,
                                              androidx.appcompat.widget.Toolbar toolbar,
                                              String displayName,
                                              Intent intent) {
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
            activity.startActivity(intent);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.finish();
        });
    }
}

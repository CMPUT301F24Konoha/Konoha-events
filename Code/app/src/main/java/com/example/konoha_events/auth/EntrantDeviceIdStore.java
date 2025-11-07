package com.example.konoha_events.auth;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;
public class EntrantDeviceIdStore{
    private static final String PREFS = "entrant_device_id_prefs";
    private static final String KEY_ID = "entrant_device_id";

    // we check if we already have an id saved
    public static boolean hasId(Context ctx){
        return getIdOrNull(ctx) != null;
    }

    //gets a saved id or creates one if there isnt any
    public static String getOrCreateId(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String existing = sp.getString(KEY_ID, null);
        if (existing != null && !existing.isEmpty()) return existing;

        String generated = UUID.randomUUID().toString();
        sp.edit().putString(KEY_ID, generated).apply();
        return generated;
    }

    // gets the saved id or NULL
    public static String getIdOrNull(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(KEY_ID, null);
    }

    // delete for testing purposes
    public static void reset(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().remove(KEY_ID).apply();
    }
}


package com.example.todosimple;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREFS_NAME = "app_prefs";
    private static final String PREF_HINT_SHOWN = "hint_shown";

    public static void setHintShown(Context context, boolean isShown) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_HINT_SHOWN, isShown);
        editor.apply();
    }

    public static boolean isHintShown(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREF_HINT_SHOWN, false);
    }
}

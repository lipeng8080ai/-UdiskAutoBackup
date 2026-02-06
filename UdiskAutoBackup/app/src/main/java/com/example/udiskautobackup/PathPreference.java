package com.example.udiskautobackup;

import android.content.Context;
import android.content.SharedPreferences;

public class PathPreference {
    private static final String PREFS_NAME = "UdiskAutoBackupPrefs";
    private static final String KEY_BACKUP_PATH = "backup_path";
    private final SharedPreferences prefs;

    public PathPreference(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setBackupPath(String path) {
        prefs.edit().putString(KEY_BACKUP_PATH, path).apply();
    }

    public String getBackupPath() {
        return prefs.getString(KEY_BACKUP_PATH, "");
    }

    public static String getBackupPath(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_BACKUP_PATH, "");
    }
}
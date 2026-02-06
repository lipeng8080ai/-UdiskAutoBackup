package com.example.udiskautobackup;

import android.content.Context;
import android.content.SharedPreferences;

public class PathPreference {
    private static final String PREF_NAME = "UdiskAutoBackupPrefs";
    private static final String KEY_BACKUP_PATH = "backup_path";
    private static final String DEFAULT_BACKUP_PATH = "/storage/emulated/0/UdiskBackup";
    
    private SharedPreferences prefs;
    
    public PathPreference(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public String getBackupPath() {
        return prefs.getString(KEY_BACKUP_PATH, DEFAULT_BACKUP_PATH);
    }
    
    public void setBackupPath(String path) {
        prefs.edit().putString(KEY_BACKUP_PATH, path).apply();
    }
}
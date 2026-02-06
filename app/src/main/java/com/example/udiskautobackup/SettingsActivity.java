package com.example.udiskautobackup;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;

public class SettingsActivity {
    private static final String PREFS_NAME = "UdiskAutoBackupPrefs";
    private static final String KEY_BACKUP_PATH_URI = "backup_path_uri";
    
    public static void saveBackupPathUri(Context context, Uri uri) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_BACKUP_PATH_URI, uri.toString()).apply();
    }
    
    public static Uri getBackupPathUri(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uriString = prefs.getString(KEY_BACKUP_PATH_URI, null);
        if (uriString != null) {
            return Uri.parse(uriString);
        }
        return null;
    }
    
    public static DocumentFile getBackupDirectory(Context context) {
        Uri backupUri = getBackupPathUri(context);
        if (backupUri != null) {
            return DocumentFile.fromTreeUri(context, backupUri);
        }
        // 默认使用应用私有目录
        return DocumentFile.fromFile(context.getExternalFilesDir("udisk_backup"));
    }
}
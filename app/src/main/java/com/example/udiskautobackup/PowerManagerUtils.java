package com.example.udiskautobackup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

public class PowerManagerUtils {
    
    public static void requestIgnoreBatteryOptimization(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(activity.getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                activity.startActivityForResult(intent, requestCode);
            }
        }
    }
    
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true;
    }
}
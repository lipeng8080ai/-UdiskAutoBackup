package com.example.udiskautobackup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UsbReceiver extends BroadcastReceiver {
    private static final String TAG = "UsbReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "USB事件: " + action);
        
        if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {
            // 启动备份服务
            Intent serviceIntent = new Intent(context, UsbMonitorService.class);
            serviceIntent.setAction("USB_ATTACHED");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
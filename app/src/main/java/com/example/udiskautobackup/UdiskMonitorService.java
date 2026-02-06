package com.example.udiskautobackup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdiskMonitorService extends Service {
    private static final String TAG = "UdiskMonitorService";
    private static final String CHANNEL_ID = "udisk_backup_channel";
    private static final int NOTIFICATION_ID = 1;
    
    private ExecutorService executorService;
    private UsbDeviceReceiver usbReceiver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
        createNotificationChannel();
        
        // 注册USB设备广播接收器
        usbReceiver = new UsbDeviceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addDataScheme("file");
        registerReceiver(usbReceiver, filter);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 创建前台服务通知（安卓8.0+必需）
        startForeground(NOTIFICATION_ID, createNotification());
        Log.d(TAG, "Udisk Monitor Service started");
        return START_STICKY; // 服务被杀死后自动重启
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (usbReceiver != null) {
            unregisterReceiver(usbReceiver);
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        Log.d(TAG, "Udisk Monitor Service destroyed");
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "U盘自动备份",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("监控U盘并自动备份文件");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("U盘自动备份")
            .setContentText("正在后台监控U盘设备")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true);
            
        return builder.build();
    }
    
    private class UsbDeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received action: " + action);
            
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                String path = intent.getData().getPath();
                Log.d(TAG, "U盘挂载路径: " + path);
                
                // 在后台线程中执行复制操作
                executorService.execute(() -> {
                    copyUdiskContent(path);
                });
            }
        }
    }
    
    private void copyUdiskContent(String udiskPath) {
        try {
            File udiskDir = new File(udiskPath);
            if (!udiskDir.exists() || !udiskDir.canRead()) {
                Log.e(TAG, "U盘路径无效或无法读取: " + udiskPath);
                return;
            }
            
            // 获取目标备份目录
            File backupDir = getBackupDirectory();
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            Log.d(TAG, "开始复制U盘内容到: " + backupDir.getAbsolutePath());
            
            // 执行复制操作
            FileUtils.copyDirectory(udiskDir, backupDir);
            
            Log.d(TAG, "U盘内容复制完成");
            
        } catch (Exception e) {
            Log.e(TAG, "复制U盘内容时出错", e);
        }
    }
    
    private File getBackupDirectory() {
        // 使用应用私有目录，避免权限问题
        return new File(getExternalFilesDir(null), "udisk_backup");
    }
}
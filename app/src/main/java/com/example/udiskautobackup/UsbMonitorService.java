package com.example.udiskautobackup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsbMonitorService extends Service {
    private static final String TAG = "UsbMonitorService";
    private static final String CHANNEL_ID = "udisk_backup_channel";
    private static final int NOTIFICATION_ID = 1;
    
    private ExecutorService executorService;
    private PathPreference pathPreference;
    
    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
        pathPreference = new PathPreference(this);
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 创建前台服务通知
        startForeground(NOTIFICATION_ID, createNotification());
        Log.d(TAG, "USB监控服务已启动");
        
        // 如果有传入的U盘路径，立即处理
        if (intent != null && intent.hasExtra("usb_path")) {
            String usbPath = intent.getStringExtra("usb_path");
            if (usbPath != null && !usbPath.isEmpty()) {
                executorService.execute(() -> {
                    copyUdiskContent(usbPath);
                });
            }
        }
        
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        Log.d(TAG, "USB监控服务已停止");
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
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, settingsIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("U盘自动备份")
            .setContentText("点击设置备份路径")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true);
            
        return builder.build();
    }
    
    public void copyUdiskContent(String udiskPath) {
        try {
            File udiskDir = new File(udiskPath);
            if (!udiskDir.exists() || !udiskDir.canRead()) {
                Log.e(TAG, "U盘路径无效或无法读取: " + udiskPath);
                return;
            }
            
            // 获取用户设置的备份目录
            String backupPath = pathPreference.getBackupPath();
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                if (!backupDir.mkdirs()) {
                    Log.e(TAG, "无法创建备份目录: " + backupPath);
                    return;
                }
            }
            
            Log.d(TAG, "开始复制U盘内容到: " + backupPath);
            
            // 执行复制操作
            FileUtils.copyDirectory(udiskDir, backupDir);
            
            Log.d(TAG, "U盘内容复制完成");
            
        } catch (Exception e) {
            Log.e(TAG, "复制U盘内容时出错", e);
        }
    }
}
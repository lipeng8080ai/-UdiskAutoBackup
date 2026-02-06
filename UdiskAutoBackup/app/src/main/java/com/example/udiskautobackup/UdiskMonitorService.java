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
import androidx.documentfile.provider.DocumentFile;
import java.io.File;

public class UdiskMonitorService extends Service {
    private static final String TAG = "UdiskMonitorService";
    private static final int NOTIFY_ID = 1001;
    private static final String CHANNEL_ID = "UdiskBackupChannel";
    private UsbReceiver usbReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "服务创建，注册U盘广播接收器");
        // 创建通知渠道，启动前台服务（AndroidO+要求）
        createNotificationChannel();
        startForeground(NOTIFY_ID, getDefaultNotification());
        // 注册U盘挂载/卸载广播
        usbReceiver = new UsbReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(usbReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // 服务被杀死后自动重启
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "服务销毁，注销广播接收器");
        unregisterReceiver(usbReceiver);
    }

    // U盘广播接收器
    private class UsbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String udiskPath = intent.getData() != null ? intent.getData().getPath() : "";
            Log.d(TAG, "收到U盘事件：" + action + "，路径：" + udiskPath);
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action) && !udiskPath.isEmpty()) {
                // U盘挂载成功，执行备份
                copyUdiskContent(udiskPath);
            }
        }
    }

    // 复制U盘内容（适配Android10+）
    private void copyUdiskContent(String udiskPath) {
        try {
            File udiskDir = new File(udiskPath);
            if (!udiskDir.exists() || !udiskDir.canRead()) {
                Log.e(TAG, "U盘路径无效/无法读取：" + udiskPath);
                return;
            }
            Log.d(TAG, "开始备份U盘内容：" + udiskPath);

            // 适配Android10+：优先使用DocumentFile，否则用传统File
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                DocumentFile backupDocDir = SettingsActivity.getBackupDir(this);
                FileUtils.copyDirWithDoc(this, udiskDir, backupDocDir);
            } else {
                File backupDir = new File(PathPreference.getBackupPath(this));
                if (backupDir.exists() || backupDir.mkdirs()) {
                    FileUtils.copyDirectory(udiskDir, backupDir);
                }
            }
            Log.d(TAG, "U盘内容备份完成");
        } catch (Exception e) {
            Log.e(TAG, "备份U盘失败", e);
        }
    }

    // 创建通知渠道
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "U盘自动备份服务", NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    // 默认前台服务通知
    private Notification getDefaultNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        return builder.setContentTitle("U盘自动备份")
                .setContentText("正在后台监控U盘状态")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
    }

    // 获取备份目录（兼容旧版）
    private File getBackupDirectory() {
        String path = PathPreference.getBackupPath(this);
        if (path == null || path.isEmpty()) {
            path = Environment.getExternalStorageDirectory() + "/UdiskBackup/";
        }
        File dir = new File(path);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }
}
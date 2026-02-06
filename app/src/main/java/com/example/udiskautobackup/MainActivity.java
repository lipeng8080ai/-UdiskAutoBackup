package com.example.udiskautobackup;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQ_PERM = 100;
    private static final int REQ_IGNORE_BATTERY = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定按钮
        Button btnStart = findViewById(R.id.buttonStartService);
        Button btnSetting = findViewById(R.id.buttonSettings);
        btnStart.setOnClickListener(v -> startBackupService());
        btnSetting.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        // 申请权限+忽略电池优化
        requestAllPermissions();
        requestIgnoreBatteryOptimization();
    }

    // 申请所有存储权限（适配Android10+/13+）
    private void requestAllPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO, android.Manifest.permission.READ_MEDIA_AUDIO};
        } else {
            permissions = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }

        // Android11+ 申请所有文件访问权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQ_PERM);
                return;
            }
        }

        // 检查普通权限
        boolean needPerm = false;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                needPerm = true;
                break;
            }
        }
        if (needPerm) {
            ActivityCompat.requestPermissions(this, permissions, REQ_PERM);
        } else {
            startBackupService();
        }
    }

    // 申请忽略电池优化（后台服务保活）
    private void requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQ_IGNORE_BATTERY);
            }
        }
    }

    // 启动U盘监控服务
    private void startBackupService() {
        Intent serviceIntent = new Intent(this, UdiskMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Toast.makeText(this, "U盘监控服务已启动", Toast.LENGTH_SHORT).show();
    }

    // 补全权限/电池优化回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_IGNORE_BATTERY || requestCode == REQ_PERM) {
            startBackupService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERM) {
            boolean allGrant = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGrant = false;
                    break;
                }
            }
            if (allGrant) {
                startBackupService();
            } else {
                Toast.makeText(this, "请授予所有存储权限，否则服务无法运行", Toast.LENGTH_LONG).show();
            }
        }
    }
}
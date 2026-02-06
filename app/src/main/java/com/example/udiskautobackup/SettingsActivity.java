package com.example.udiskautobackup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

public class SettingsActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_DIRECTORY = 1001;
    private EditText editTextBackupPath;
    private PathPreference pathPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        pathPreference = new PathPreference(this);
        editTextBackupPath = findViewById(R.id.editTextBackupPath);
        Button buttonSelectPath = findViewById(R.id.buttonSelectPath);
        Button buttonSave = findViewById(R.id.buttonSave);

        // 显示已保存的备份路径
        editTextBackupPath.setText(pathPreference.getBackupPath());

        // 选择备份文件夹
        buttonSelectPath.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_CODE_PICK_DIRECTORY);
        });

        // 保存备份路径
        buttonSave.setOnClickListener(v -> {
            String newPath = editTextBackupPath.getText().toString().trim();
            if (!newPath.isEmpty()) {
                pathPreference.setBackupPath(newPath);
                Toast.makeText(this, "备份路径保存成功", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "备份路径不能为空", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_DIRECTORY && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // 持久化文件夹访问权限
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                DocumentFile docFile = DocumentFile.fromTreeUri(this, uri);
                if (docFile != null) {
                    editTextBackupPath.setText(docFile.getUri().getPath());
                    saveBackupPathUri(this, uri);
                }
            }
        }
    }

    // 保存/获取备份路径URI（适配Android10+）
    private static final String PREFS_NAME = "UdiskAutoBackupPrefs";
    private static final String KEY_BACKUP_URI = "backup_path_uri";
    public static void saveBackupPathUri(Context context, Uri uri) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_BACKUP_URI, uri.toString()).apply();
    }
    public static Uri getBackupPathUri(Context context) {
        String uriStr = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_BACKUP_URI, null);
        return uriStr == null ? null : Uri.parse(uriStr);
    }
    public static DocumentFile getBackupDir(Context context) {
        Uri uri = getBackupPathUri(context);
        if (uri != null) return DocumentFile.fromTreeUri(context, uri);
        return DocumentFile.fromFile(context.getExternalFilesDir("udisk_backup"));
    }
}
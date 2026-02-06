package com.example.udiskautobackup;

import android.content.Context;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    // 传统File复制（适配Android9及以下）
    public static void copyDirectory(File sourceDir, File targetDir) throws IOException {
        if (!targetDir.exists()) targetDir.mkdirs();
        File[] files = sourceDir.listFiles();
        if (files == null) return;
        for (File file : files) {
            File targetFile = new File(targetDir, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, targetFile);
            } else {
                copyFile(file, targetFile);
            }
        }
    }
    public static void copyFile(File source, File target) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[1024 * 4];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }

    // DocumentFile复制（适配Android10+）
    public static void copyFileUsingDoc(Context context, Uri sourceUri, DocumentFile targetFile) throws IOException {
        try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
             OutputStream out = context.getContentResolver().openOutputStream(targetFile.getUri())) {
            byte[] buffer = new byte[1024 * 4];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
    public static void copyDirWithDoc(Context context, File sourceDir, DocumentFile targetDocDir) throws IOException {
        File[] files = sourceDir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                DocumentFile newDocDir = targetDocDir.createDirectory(file.getName());
                if (newDocDir != null) copyDirWithDoc(context, file, newDocDir);
            } else {
                DocumentFile newDocFile = targetDocDir.createFile(getMimeType(file), file.getName());
                if (newDocFile != null) copyFileUsingDoc(context, Uri.fromFile(file), newDocFile);
            }
        }
    }

    // 获取文件MIME类型
    public static String getMimeType(File file) {
        String ext = getFileExtension(file);
        android.webkit.MimeTypeMap map = android.webkit.MimeTypeMap.getSingleton();
        String type = map.getMimeTypeFromExtension(ext);
        return type == null ? "application/octet-stream" : type;
    }
    public static String getFileExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        return dotIndex == -1 ? "" : name.substring(dotIndex + 1).toLowerCase();
    }
}
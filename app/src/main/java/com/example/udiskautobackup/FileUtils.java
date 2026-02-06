package com.example.udiskautobackup;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {
    private static final String TAG = "FileUtils";
    
    public static void copyDirectory(File source, File target) throws IOException {
        if (!source.exists()) {
            throw new IOException("源目录不存在: " + source.getAbsolutePath());
        }
        
        if (!target.exists()) {
            if (!target.mkdirs()) {
                throw new IOException("无法创建目标目录: " + target.getAbsolutePath());
            }
        }
        
        File[] files = source.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            File targetFile = new File(target, file.getName());
            
            if (file.isDirectory()) {
                copyDirectory(file, targetFile);
            } else {
                copyFile(file, targetFile);
            }
        }
    }
    
    public static void copyFile(File source, File target) throws IOException {
        Log.d(TAG, "复制文件: " + source.getName());
        
        try (FileChannel inputChannel = new FileInputStream(source).getChannel();
             FileChannel outputChannel = new FileOutputStream(target).getChannel()) {
            
            long size = inputChannel.size();
            long transferred = 0;
            
            while (transferred < size) {
                transferred += outputChannel.transferFrom(inputChannel, transferred, 
                    Math.min(1024 * 1024, size - transferred));
            }
        }
    }
}
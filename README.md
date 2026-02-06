# U盘自动备份应用

这是一个完全离线、后台隐藏运行的安卓应用，用于自动将插入的U盘内容复制到指定文件夹。

## 功能特点

- ✅ **完全离线**：不依赖网络，不上传任何数据
- ✅ **后台隐藏**：无界面显示，静默运行
- ✅ **自动触发**：检测到U盘插入自动开始复制
- ✅ **自动备份**：将U盘所有内容复制到应用私有目录
- ✅ **低功耗**：使用前台服务确保后台持续运行

## 备份位置

备份文件存储在：
```
/storage/emulated/0/Android/data/com.example.udiskautobackup/files/udisk_backup/
```

## 使用说明

### 1. 编译安装
```bash
# 克隆项目
git clone [项目地址]

# 使用Android Studio打开项目并编译
# 或使用命令行构建
./gradlew assembleDebug
```

### 2. 首次运行设置
- 安装APK后首次启动会请求必要权限
- 需要授予存储权限
- 需要允许忽略电池优化（防止被系统杀死）

### 3. 隐藏应用图标（可选）
如果需要完全隐藏应用图标，可以使用以下ADB命令：
```bash
adb shell pm hide com.example.udiskautobackup
```

要恢复显示：
```bash
adb shell pm unhide com.example.udiskautobackup
```

## 系统兼容性

- **最低支持**：Android 5.0 (API 21)
- **最佳体验**：Android 8.0+ (支持前台服务)
- **存储权限**：适配Android 10+ Scoped Storage

## 注意事项

1. **USB OTG支持**：确保设备支持USB OTG功能
2. **电池优化**：必须关闭电池优化，否则服务会被系统杀死
3. **存储空间**：确保设备有足够空间存储备份文件
4. **U盘格式**：支持FAT32、exFAT、NTFS格式的U盘

## 技术实现

- 使用BroadcastReceiver监听USB设备挂载事件
- 前台服务确保后台持续运行
- 文件通道(FileChannel)高效复制大文件
- 应用私有目录避免权限问题

## 许可证

MIT License - 完全开源，可自由修改和分发
package com.example.myapp;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 2000; // 2秒延迟
    private static final int REQUEST_CODE_PERMISSION_SETTINGS = 1001;

    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Runnable permissionCheckTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // 软件需要检查权限：通知，后台运行
        // 延迟执行权限检查任务
        uiHandler.postDelayed(() -> {
            checkRequiredPermissionsAndProceed();
        }, 500); // 短暂延迟让 UI 加载更顺畅
    }
    private void checkRequiredPermissionsAndProceed() {
        List<String> missingPermissions = getMissingPermissions(this);

        if (missingPermissions.isEmpty()) {
            proceedToMainActivity();
            return;
        }

        showPermissionDialog(missingPermissions);
    }
    private List<String> getMissingPermissions(Context context) {
        List<String> missing = new ArrayList<>();

        // 检查通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !isNotificationPermissionGranted(context)) {
            missing.add("通知权限");
        }

        // 检查忽略电池优化权限（后台运行）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !isBatteryOptimizationEnabled(context)) {
            missing.add("后台运行权限");
        }

        return missing;
    }

    public static boolean isNotificationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED;
        } else {
            // 在 Android 12 及以下版本，通知权限默认已被授予
            return true;
        }

    }
    public static boolean isBatteryOptimizationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                return pm.isIgnoringBatteryOptimizations(context.getPackageName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    private void showPermissionDialog(List<String> missingPermissions) {
        StringBuilder message = new StringBuilder("应用需要以下权限以正常使用：\n\n");
        for (String perm : missingPermissions) {
            message.append("- ").append(perm).append("\n");
        }
        message.append("\n请前往设置开启这些权限。");

        new AlertDialog.Builder(this)
                .setTitle("缺少必要权限")
                .setMessage(message.toString())
                .setPositiveButton("前往设置", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_CODE_PERMISSION_SETTINGS);
                })
                .setCancelable(false) // 阻止取消
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PERMISSION_SETTINGS) {
            checkRequiredPermissionsAndProceed(); // 返回后重新检查权限
        }
    }

    private void proceedToMainActivity() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 1000);
    }
} 
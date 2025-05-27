package com.example.myapp;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 2000; // 2秒延迟

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // 检查并请求通知策略权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            /*
              检查通知策略权限
              其中notificationManager != null 是为了避免在 Android 9.0（API 28）及以上版本中，获取通知策略权限时出现权限被禁用的异常
              !notificationManager.isNotificationPolicyAccessGranted()是检查你的应用是否被用户授权修改“请勿打扰”（Do Not Disturb）模式的权限，已授权返回false不必申请
             */
            if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
                Toast toast = Toast.makeText(WelcomeActivity.this, "请开启‘请勿打扰’权限", Toast.LENGTH_SHORT);
                toast.show();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // 延迟跳转系统设置页面
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
                }, 1500);
            }
        }

        new Handler().postDelayed(() -> {
            // 延迟后跳转到主界面
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
} 
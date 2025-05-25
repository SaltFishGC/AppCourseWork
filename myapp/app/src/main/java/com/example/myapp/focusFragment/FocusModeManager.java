package com.example.myapp.focusFragment;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.myapp.R;

public class FocusModeManager {
    private static final String CHANNEL_ID = "focus_mode_channel";
    private static final int NOTIFICATION_ID = 1;
    private Context context;
    private AlarmManager alarmManager;
    private NotificationManager notificationManager;

    public FocusModeManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "专注模式",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("显示专注模式的运行状态");
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void startFocusMode(int durationMinutes) {
        // 创建前台服务
        Intent serviceIntent = new Intent(context, FocusModeService.class);
        serviceIntent.putExtra("duration", durationMinutes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        // 设置定时器
        long endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000);
        Intent intent = new Intent(context, FocusModeReceiver.class);
        intent.setAction("com.example.myapp.FOCUS_MODE_END");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                endTime,
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                endTime,
                pendingIntent
            );
        }
    }

    public void stopFocusMode() {
        // 停止服务
        Intent serviceIntent = new Intent(context, FocusModeService.class);
        context.stopService(serviceIntent);

        // 取消定时器
        Intent intent = new Intent(context, FocusModeReceiver.class);
        intent.setAction("com.example.myapp.FOCUS_MODE_END");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    public void showFocusModeNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_focus_mode)
            .setContentTitle("专注模式")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
} 
package com.example.myapp.focusFragment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;

import com.example.myapp.MainActivity;
import com.example.myapp.R;

public class FocusModeService extends Service {
    private static final String CHANNEL_ID = "focus_mode_channel";
    private static final int NOTIFICATION_ID = 1;
    private final IBinder binder = new LocalBinder();
    private int durationMinutes;
    private PowerManager.WakeLock wakeLock;
    private boolean isRunning = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    public class LocalBinder extends Binder {
        FocusModeService getService() {
            return FocusModeService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        acquireWakeLock();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            durationMinutes = intent.getIntExtra("duration", 25);
            startForeground(NOTIFICATION_ID, createNotification());
            isRunning = true;
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
        isRunning = false;
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                "MyApp::FocusModeWakeLock"
            );
            wakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "专注模式",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("显示专注模式的运行状态");
            channel.setShowBadge(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, FocusModeReceiver.class);
        stopIntent.setAction("com.example.myapp.FOCUS_MODE_END");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("专注模式运行中")
            .setContentText(String.format("剩余时间：%d分钟", durationMinutes))
            .setSmallIcon(R.drawable.ic_focus_mode)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_focus_mode, "停止", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    public void updateNotification(int remainingMinutes) {
        durationMinutes = remainingMinutes;
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
} 
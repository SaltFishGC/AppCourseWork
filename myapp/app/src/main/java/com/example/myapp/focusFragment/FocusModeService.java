package com.example.myapp.focusFragment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;

import com.example.myapp.MainActivity;
import com.example.myapp.R;

public class FocusModeService extends Service {
    private static final String CHANNEL_ID = "focus_mode_channel";
    private static final int NOTIFICATION_ID = 1;
    private final IBinder binder = new LocalBinder();
    private PowerManager.WakeLock wakeLock;
    private boolean isRunning = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int remainingSeconds = 0;
    private Runnable countdownRunnable;
    private long startTime; // 服务启动的时间
    private boolean isCountdownMode = true; // 默认为倒计时模式


    private void startCountdown(int totalSeconds) {
        remainingSeconds = totalSeconds;
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (remainingSeconds > 0 && isRunning) {
                    remainingSeconds--;
                    updateNotification(remainingSeconds / 60);
                    handler.postDelayed(this, 1000); // 每秒更新一次
                } else if (isRunning) {
                    stopSelf(); // 倒计时结束，自动停止服务
                }
            }
        };
        handler.post(countdownRunnable);
    }
    private void startTimerOnly() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    remainingSeconds++;
                    updateNotification(remainingSeconds);
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }



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
            int duration = intent.getIntExtra("duration", -1); // -1 表示计时模式
            isCountdownMode = (duration > 0);

            if (isCountdownMode) {
                remainingSeconds = duration;
                startForeground(NOTIFICATION_ID, createNotification());
                isRunning = true;
                startCountdown(duration);
            } else {
                startTime = System.currentTimeMillis();
                remainingSeconds = 0;
                startForeground(NOTIFICATION_ID, createNotification());
                isRunning = true;
                startTimerOnly(); // 新增方法
            }
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
                Log.d("FocusService", "创建成功: "+channel.getName());
            }
        }
    }

    private Notification createNotification() {
        String contentText;
        if (isCountdownMode && remainingSeconds > 0) {
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            contentText = String.format("剩余时间：%02d:%02d", minutes, seconds);
        } else if (isCountdownMode) {
            contentText = "专注已完成";
        } else {
            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            int minutes = (int) (elapsedSeconds / 60);
            int seconds = (int) (elapsedSeconds % 60);
            contentText = String.format("已专注：%02d:%02d", minutes, seconds);
        }


        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setComponent(new ComponentName(this, MainActivity.class));//用ComponentName得到class对象
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("专注模式运行中")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_focus_mode)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }


    public void updateNotification(int remainingMinutes) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
} 
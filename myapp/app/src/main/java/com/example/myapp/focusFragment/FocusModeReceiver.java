package com.example.myapp.focusFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class FocusModeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.example.myapp.FOCUS_MODE_END".equals(intent.getAction())) {
            // 停止专注模式服务
            Intent serviceIntent = new Intent(context, FocusModeService.class);
            context.stopService(serviceIntent);

            // 恢复系统声音
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }

            // 发送专注模式结束的通知
            FocusModeManager focusModeManager = new FocusModeManager(context);
            focusModeManager.showFocusModeNotification("专注模式已结束");
        }
    }
} 
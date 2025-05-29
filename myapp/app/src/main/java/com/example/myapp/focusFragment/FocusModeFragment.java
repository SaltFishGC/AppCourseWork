package com.example.myapp.focusFragment;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieDrawable;
import com.example.myapp.model.SharedViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.airbnb.lottie.LottieAnimationView;


import com.example.myapp.R;
import com.example.myapp.MainActivity;
import com.example.myapp.dao.TimeLearnedDao;
import com.example.myapp.entity.TimeLearned;

import java.util.Date;

public class FocusModeFragment extends Fragment {
    private static final int REQUEST_CODE_NOTIFICATION_POLICY = 1234;

    private TextView timerText;
    private MaterialButton startButton;
    private MaterialButton stopButton;
    private MaterialButton modeSwitchButton;
    private Slider durationSlider;
    private TextView durationText;
    private SwitchMaterial silentSwitch;
    private TextView totalFocusTimeText;
    
    private FocusModeService focusModeService;
    private boolean isBound = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int remainingSeconds = 0;
    private boolean isRunning = false;
    private boolean isCountdownMode = true; // true为倒计时模式，false为计时模式
    private int totalFocusSeconds = 0; // 记录总专注时长
    private int initialDuration = 0; // 记录开始时的倒计时时长
    private TimeLearnedDao timeLearnedDao;
    private Runnable pendingRingerModeChange;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FocusModeService.LocalBinder binder = (FocusModeService.LocalBinder) service;
            focusModeService = binder.getService();
            isBound = true;
            // 通知MainActivity更新导航栏状态
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setFocusModeService(focusModeService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            focusModeService = null;
            isBound = false;
            // 通知MainActivity更新导航栏状态
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setFocusModeService(null);
            }
        }
    };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_focus_mode, container, false);

        //  初始化视图
        initViews(view);

        // 初始化数据库
        timeLearnedDao = new TimeLearnedDao(requireContext());
        timeLearnedDao.open();

        // 更新总专注时长显示
        updateTotalFocusTimeDisplay();

        // 设置监听器
        setupListener();
        
        return view;
    }

    private void initViews(View view) {
        // 初始化视图
        timerText = view.findViewById(R.id.timer_text);
        startButton = view.findViewById(R.id.start_button);
        stopButton = view.findViewById(R.id.stop_button);
        modeSwitchButton = view.findViewById(R.id.mode_switch_button);
        durationSlider = view.findViewById(R.id.duration_slider);
        durationText = view.findViewById(R.id.duration_text);
        silentSwitch = view.findViewById(R.id.silent_switch);
        totalFocusTimeText = view.findViewById(R.id.total_focus_time_text);
    }

    private void setupListener() {
        // 设置滑块监听
        durationSlider.addOnChangeListener((slider, value, fromUser) -> {
            int minutes = (int) value;
            durationText.setText(String.format("专注时长：%d分钟", minutes));
            if (isCountdownMode) {
                updateTimerDisplay(minutes * 60);
            }
        });

        // 设置开始按钮点击事件
        startButton.setOnClickListener(v -> startFocusMode());

        // 设置停止按钮点击事件
        stopButton.setOnClickListener(v -> stopFocusMode());

        // 设置模式切换按钮点击事件
        modeSwitchButton.setOnClickListener(v -> switchMode());

        // 设置静音模式切换按钮点击事件
        silentSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> switchSilentMode(isChecked));
    }

    private void startFocusMode() {
        if (isCountdownMode) {
            initialDuration = (int) durationSlider.getValue() * 60;
            remainingSeconds = initialDuration;
        } else {
            remainingSeconds = 0;
        }

        // 启动服务
        Intent intent = new Intent(getActivity(), FocusModeService.class);
        intent.putExtra("duration", isCountdownMode ? remainingSeconds : -1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(intent);
        } else {
            getActivity().startService(intent);
        }

        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        
        // 设置静音模式
        if (silentSwitch.isChecked()) {
            checkAndRequestNotificationPolicyPermission(() -> {
                AudioManager audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
            });
        }

        // 更新UI
        startButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.VISIBLE);
        modeSwitchButton.setVisibility(View.GONE);
        durationSlider.setEnabled(false);
        isRunning = true;
        
        // 开始计时
        startTimer();
    }

    private void stopFocusMode() {
        // 检查 Context 是否有效
        if (getActivity() == null) return;

        // 恢复声音
        checkAndRequestNotificationPolicyPermission(() -> {
            AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        });

        // 解绑服务前检查是否已绑定
        if (isBound) {
            try {
                getActivity().unbindService(connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isBound = false;
        }

        // 停止服务
        getActivity().stopService(new Intent(getActivity(), FocusModeService.class));

        // 更新UI
        startButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.GONE);
        modeSwitchButton.setVisibility(View.VISIBLE);
        durationSlider.setEnabled(true);
        isRunning = false;

        // 停止计时
        handler.removeCallbacksAndMessages(null);

        // 显示专注时长统计
        showFocusStats();

        // 重置计时器显示
        if (isCountdownMode) {
            updateTimerDisplay((int) durationSlider.getValue() * 60);
        } else {
            updateTimerDisplay(0);
        }
    }


    private void switchMode() {
        isCountdownMode = !isCountdownMode;
        if (isCountdownMode) {
            modeSwitchButton.setText("切换到计时模式");
            durationSlider.setVisibility(View.VISIBLE);
            durationText.setVisibility(View.VISIBLE);
            updateTimerDisplay((int) durationSlider.getValue() * 60);
        } else {
            modeSwitchButton.setText("切换到倒计时模式");
            durationSlider.setVisibility(View.GONE);
            durationText.setVisibility(View.GONE);
            updateTimerDisplay(0);
        }
    }

    private void switchSilentMode(boolean isChecked) {
        if (isRunning) {
            if (isChecked) {
                // 开启静音
                checkAndRequestNotificationPolicyPermission(() -> {
                    AudioManager audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null) {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    }
                });
            } else {
                // 关闭静音，恢复铃声
                checkAndRequestNotificationPolicyPermission(() -> {
                    AudioManager audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null) {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    }
                });
            }
        }
    }

    private void startTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    if (isCountdownMode) { // 倒计时模式
                        if (remainingSeconds > 0) { // 倒计时进行中
                            remainingSeconds--;
                            updateTimerDisplay(remainingSeconds);
                            handler.postDelayed(this, 1000);
                        } else { // 倒计时结束
                            // 震动提醒
                            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
                            if (vibrator != null && vibrator.hasVibrator()) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    vibrator.vibrate(1000);
                                }
                            }
                            // 停止专注模式
                            stopFocusMode();

                        }
                    } else { // 计时模式
                        remainingSeconds++;
                        totalFocusSeconds++;
                        updateTimerDisplay(remainingSeconds);
                        handler.postDelayed(this, 1000);
                    }
                }
            }
        });
    }

    private void updateTimerDisplay(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        timerText.setText(String.format("%02d:%02d", minutes, remainingSeconds));
    }

    public void updateTotalFocusTimeDisplay() {
        double totalMinutes = timeLearnedDao.getTotalTimeLearned();
        int hours = (int) (totalMinutes / 60);
        int minutes = (int) (totalMinutes % 60);
        totalFocusTimeText.setText(String.format("总专注时长：%d小时%d分钟", hours, minutes));
    }

    private void showFocusStats() {
        int minutes = totalFocusSeconds / 60;
        int hours = minutes / 60;
        minutes = minutes % 60;
        
        int focusDuration;
        if (isCountdownMode) {
            focusDuration = initialDuration - remainingSeconds;
        } else {
            focusDuration = remainingSeconds;
        }
        
        // 保存本次专注记录到数据库
        TimeLearned timeLearned = new TimeLearned(focusDuration / 60.0, new Date());
        timeLearnedDao.insert(timeLearned);
        
        // 更新总专注时长显示
        updateTotalFocusTimeDisplay();
        
        // 创建自定义对话框布局
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_well_done, null);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        LottieAnimationView confettiView = dialogView.findViewById(R.id.confetti_animation);

        // 设置文本内容
        titleView.setText("专注完成！");
        messageView.setText(String.format("本次专注时长：%02d:%02d\n总专注时长：%d小时%02d分钟", 
            focusDuration / 60, focusDuration % 60,
            hours, minutes));

        // 设置动画
        confettiView.setAnimation(R.raw.confetti);
        confettiView.setRepeatCount(LottieDrawable.INFINITE);
        confettiView.setSpeed(1.0f);  // 设置动画速度
        confettiView.playAnimation();

        // 创建对话框
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
        
        // 4秒后自动关闭对话框
        dialogView.postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 4000);
    }

    /**
     * 检查并请求修改铃声模式的权限（Do Not Disturb）
     * 如果已有权限，执行 onGranted 回调
     */
    private void checkAndRequestNotificationPolicyPermission(Runnable onGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager =
                (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                pendingRingerModeChange = onGranted;
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivityForResult(intent, REQUEST_CODE_NOTIFICATION_POLICY);
            } else {
                if (onGranted != null) {
                    onGranted.run();
                }
            }
        } else {
            if (onGranted != null) {
                onGranted.run();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_NOTIFICATION_POLICY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NotificationManager notificationManager =
                    (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

                if (notificationManager.isNotificationPolicyAccessGranted()) {
                    // 用户已授权，继续执行上次未完成的操作（如设置铃声模式）
                    if (pendingRingerModeChange != null) {
                        pendingRingerModeChange.run();
                        pendingRingerModeChange = null;
                    }
                } else {
                    // 用户拒绝授权，提示信息
                    Toast.makeText(requireContext(), "需要授权才能调整铃声模式", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        
        // 关闭数据库连接
        if (timeLearnedDao != null) {
            timeLearnedDao.close();
        }
        // 防止重复解绑
        if (isBound) {
            try {
                getActivity().unbindService(connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isBound = false;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (!isBound && focusModeService == null) {
            bindService(); // 重新绑定服务
        }
    }
    private void bindService() {
        Intent intent = new Intent(getActivity(), FocusModeService.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            getActivity().unbindService(connection);
            isBound = false;
        }
    }



} 
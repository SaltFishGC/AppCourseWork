package com.example.myapp.wordFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.myapp.R;
import com.example.myapp.dao.SqliteConnection;
import com.example.myapp.dao.WordDao;
import com.example.myapp.dao.WordLearningRecordDao;
import com.example.myapp.entity.Word;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.SharedPreferences;

public class WordRecitationSequentialFragment extends Fragment {
    private TextView wordText;
    private TextView definitionText;
    private TextView variantText;
    private TextView topicText;
    private TextView progressText;
    private Button btnNeverSeen;
    private Button btnForgot;
    private Button btnRemembered;
    private MaterialButton btnEnd;
    private MaterialButton btnPrev;
    private MaterialButton btnNext;
    private TextView definitionMaskText;
    private boolean isDefinitionVisible = false;
    private WordDao wordDao;
    private WordLearningRecordDao learningRecordDao;
    private int currentId;
    private Word currentWord;
    private int completedWords = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_recitation_sequential, container, false);

        // 初始化视图
        initViews(view);

        // 添加遮挡词义
        setupDefinitionMask(view);

        // 初始化数据库
        SqliteConnection dbHelper = new SqliteConnection(requireContext());
        wordDao = new WordDao(dbHelper);
        learningRecordDao = new WordLearningRecordDao(dbHelper);

        // 获取 SharedPreferences
        SharedPreferences sharedPref = requireContext().getSharedPreferences("WordRecitationPrefs", Context.MODE_PRIVATE);
        currentId = sharedPref.getInt("last_word_id", 1); // 默认从1开始

        // 加载当前单词
        loadCurrentWord();
        toggleDefinitionVisibility();

        // 设置按钮点击事件
        setupButtonListeners();

        return view;
    }

    private void initViews(View view) {
        wordText = view.findViewById(R.id.word_text);
        definitionText = view.findViewById(R.id.definition_text);
        variantText = view.findViewById(R.id.variant_text);
        topicText = view.findViewById(R.id.topic_text);
        progressText = view.findViewById(R.id.progress_text);
        btnNeverSeen = view.findViewById(R.id.btn_never_seen);
        btnForgot = view.findViewById(R.id.btn_forgot);
        btnRemembered = view.findViewById(R.id.btn_remembered);
        btnEnd = view.findViewById(R.id.btn_end);
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);
    }

    private void setupDefinitionMask(View view) {
        definitionMaskText = new TextView(requireContext());
        definitionMaskText.setText("点击以显示词义");
        definitionMaskText.setTextSize(18);
        definitionMaskText.setGravity(android.view.Gravity.CENTER);
        definitionMaskText.setBackgroundColor(getResources().getColor(android.R.color.white));
        definitionMaskText.setTextColor(getResources().getColor(android.R.color.darker_gray));

        ViewGroup parent = view.findViewById(R.id.answer);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        definitionMaskText.setLayoutParams(params);
        parent.addView(definitionMaskText, 0);

        definitionMaskText.setOnClickListener(v -> toggleDefinitionVisibility());
    }

    private void toggleDefinitionVisibility() {
        isDefinitionVisible = !isDefinitionVisible;
        definitionMaskText.setVisibility(isDefinitionVisible ? View.GONE : View.VISIBLE);
        int visibility = isDefinitionVisible ? View.VISIBLE : View.GONE;
        definitionText.setVisibility(visibility);
        variantText.setVisibility(visibility);
        topicText.setVisibility(visibility);
    }

    private void loadCurrentWord() {
        currentWord = wordDao.getWordById(currentId);
        if (currentWord == null) {
            // 单词不存在时，可能已经完成全部背诵
            showCompletionDialog();
            returnToWelcomeFragment();
            return;
        }
        updateWordDisplay();
    }

    private void updateWordDisplay() {
        if (currentWord != null) {
            wordText.setText(currentWord.getWord());
            definitionText.setText(currentWord.getDefinition());
            variantText.setText(currentWord.getVariant());
            topicText.setText(currentWord.getTopic());
            progressText.setText(String.format("目前单词序号：%d", currentId));
            toggleDefinitionVisibility();
        }
    }

    private void setupButtonListeners() {
        btnNeverSeen.setOnClickListener(v -> handleWordResponse(0));
        btnForgot.setOnClickListener(v -> handleWordResponse(1));
        btnRemembered.setOnClickListener(v -> handleWordResponse(2));
        btnEnd.setOnClickListener(v -> returnToWelcomeFragment());

        // 新增按钮点击事件
        btnPrev.setOnClickListener(v -> {
            if (currentId > 1) {
                currentId--;
                loadCurrentWord();
                // 上一个单词不用隐藏，直接显示
                if (!isDefinitionVisible)
                    toggleDefinitionVisibility();
            }
        });

        btnNext.setOnClickListener(v -> {
            currentId++;
            loadCurrentWord();
            if (isDefinitionVisible)
                toggleDefinitionVisibility();
        });
    }

    private void handleWordResponse(int response) {
        if (currentWord != null) {
            switch (response) {
                case 0: // 没见过
                case 1: // 忘记
                    // 不做任何处理，直接加载下一个单词
                    break;
                case 2: // 记得
                    wordDao.updateRememberedStatus(currentWord.getId(), 1);
                    learningRecordDao.addLearningRecord(currentWord.getId());
                    completedWords++;
                    // 更新 SharedPreferences
                    SharedPreferences sharedPref = requireContext().getSharedPreferences("WordRecitationPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("last_word_id", currentId + 1);
                    editor.apply();
                    break;
            }
            currentId++; // 下一个单词
            loadCurrentWord();
        }
    }

    private void showCompletionDialog() {
        // 创建自定义对话框布局
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_well_done, null);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        LottieAnimationView confettiView = dialogView.findViewById(R.id.confetti_animation);

        titleView.setText("背诵完成！");
        messageView.setText("所有单词已背诵完毕");
        confettiView.setAnimation(R.raw.confetti);
        confettiView.setRepeatCount(LottieDrawable.INFINITE);
        confettiView.playAnimation();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialogView.postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 4000);
    }
    private void returnToWelcomeFragment() {
        // 创建自定义对话框布局
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_well_done, null);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        LottieAnimationView confettiView = dialogView.findViewById(R.id.confetti_animation);

        // 设置文本内容
        titleView.setText("背诵结束");
        messageView.setText(String.format("本次背诵词数：%d",completedWords));

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
        // 返回欢迎页面
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_word, new WordRecitationWelcomeFragment())
                .commit();
    }
}
package com.example.myapp.wordFragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.android.material.button.MaterialButton;

import com.example.myapp.R;
import com.example.myapp.dao.SqliteConnection;
import com.example.myapp.dao.WordDao;
import com.example.myapp.dao.WordLearningRecordDao;
import com.example.myapp.entity.Word;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class WordRecitationUnlimitedFragment extends Fragment {
    private TextView wordText;
    private TextView definitionText;
    private TextView variantText;
    private TextView topicText;
    private TextView progressText;
    private Button btnNeverSeen;
    private Button btnForgot;
    private Button btnRemembered;
    private MaterialButton btnEnd;

    // 新增遮挡词义的变量
    private TextView definitionMaskText;  // 用于遮挡词义的文本
    private boolean isDefinitionVisible = false;  // 是否已显示词义

    private WordDao wordDao;
    private WordLearningRecordDao learningRecordDao;
    private Word currentWord;
    private int completedWords;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_recitation_unlimited, container, false);
        
        // 初始化视图
        initViews(view);

        // 创建遮挡词义
        setupDefinitionMask(view);

        // 初始化数据库
        SqliteConnection dbHelper = new SqliteConnection(requireContext());
        wordDao = new WordDao(dbHelper);
        learningRecordDao = new WordLearningRecordDao(dbHelper);
        
        // 加载第一个单词
        loadNextWord();
        
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
    }

    private void setupButtonListeners() {
        btnNeverSeen.setOnClickListener(v -> handleWordResponse(0));
        btnForgot.setOnClickListener(v -> handleWordResponse(1));
        btnRemembered.setOnClickListener(v -> handleWordResponse(2));
        btnEnd.setOnClickListener(v -> returnToWelcomeFragment());
    }

    // 新增方法：设置词义遮挡层
    private void setupDefinitionMask(View view) {
        // 创建用于遮挡的 TextView
        definitionMaskText = new TextView(requireContext());
        definitionMaskText.setText("点击以显示词义");
        definitionMaskText.setTextSize(18);
        definitionMaskText.setTypeface(Typeface.DEFAULT_BOLD);
        definitionMaskText.setGravity(android.view.Gravity.CENTER);
        definitionMaskText.setBackgroundColor(getResources().getColor(android.R.color.white));
        definitionMaskText.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // 设置 LayoutParams，使其填满 answer 布局
        ViewGroup parent = view.findViewById(R.id.answer);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        definitionMaskText.setLayoutParams(params);

        parent.addView(definitionMaskText, 0); // 插入到最上层

        // 设置点击事件
        definitionMaskText.setOnClickListener(v -> toggleDefinitionVisibility());
    }

    // 切换词义显示状态的方法
    private void toggleDefinitionVisibility() {
        isDefinitionVisible = !isDefinitionVisible;
        definitionMaskText.setVisibility(isDefinitionVisible ? View.GONE : View.VISIBLE);

        // 显示或隐藏 answer 区域中的各个组件
        int visibility = isDefinitionVisible ? View.VISIBLE : View.GONE;
        definitionText.setVisibility(visibility);
        variantText.setVisibility(visibility);
        topicText.setVisibility(visibility);
    }

    private void loadNextWord() {
        // 获取一个未记住的单词
        currentWord = wordDao.getRandomUnrememberedWord();
        updateWordDisplay();
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
                    break;
            }
            loadNextWord();
            toggleDefinitionVisibility();
        }
    }

    private void updateWordDisplay() {
        if (currentWord != null) {
            wordText.setText(currentWord.getWord());

            // 更新词义等字段，但保持隐藏状态
            definitionText.setText(currentWord.getDefinition());
            variantText.setText(currentWord.getVariant());
            topicText.setText(currentWord.getTopic());

            progressText.setText(String.format("已完成：%d个单词", completedWords));

            // 如果词义已显示，则刷新内容；否则保持隐藏
            if (!isDefinitionVisible) {
                definitionText.setVisibility(View.GONE);
                variantText.setVisibility(View.GONE);
                topicText.setVisibility(View.GONE);
            }
        }
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
                .replace(R.id.fragment_container, new WordRecitationWelcomeFragment())
                .commit();
    }
} 
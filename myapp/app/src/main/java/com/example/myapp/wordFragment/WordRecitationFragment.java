package com.example.myapp.wordFragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

import com.example.myapp.R;
import com.example.myapp.dao.SqliteConnection;
import com.example.myapp.dao.WordDao;
import com.example.myapp.dao.WordLearningRecordDao;
import com.example.myapp.entity.Word;

import java.util.ArrayList;
import java.util.List;
public class WordRecitationFragment extends Fragment {
    private TextView wordText;
    private TextView definitionText;
    private TextView variantText;
    private TextView topicText;
    private TextView progressText;
    private Button btnNeverSeen;
    private Button btnForgot;
    private Button btnRemembered;

    private WordDao wordDao;
    private WordLearningRecordDao learningRecordDao;
    private List<Word> wordList;
    private List<Word> reviewList;
    private int currentIndex;
    private int totalWords;
    private int completedWords;
    // 新增遮挡词义的变量
    private TextView definitionMaskText;  // 用于遮挡词义的文本
    private boolean isDefinitionVisible = false;  // 是否已显示词义

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_recitation, container, false);
        
        // 初始化视图
        initViews(view);

        // 添加遮挡词义的视图
        setupDefinitionMask(view);
        
        // 初始化数据库
        SqliteConnection dbHelper = new SqliteConnection(requireContext());
        wordDao = new WordDao(dbHelper);
        learningRecordDao = new WordLearningRecordDao(dbHelper);
        
        // 加载单词
        loadWords();
        
        // 设置按钮点击事件
        setupButtonListeners();
        
        return view;
    }

    //初始化视图
    private void initViews(View view) {
        wordText = view.findViewById(R.id.word_text);
        definitionText = view.findViewById(R.id.definition_text);
        variantText = view.findViewById(R.id.variant_text);
        topicText = view.findViewById(R.id.topic_text);
        progressText = view.findViewById(R.id.progress_text);
        btnNeverSeen = view.findViewById(R.id.btn_never_seen);
        btnForgot = view.findViewById(R.id.btn_forgot);
        btnRemembered = view.findViewById(R.id.btn_remembered);
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

    // 加载单词
    private void loadWords() {
        wordList = wordDao.getRandomUnrememberedWords();
        reviewList = new ArrayList<>();
        currentIndex = 0;
        totalWords = wordList.size();
        completedWords = 0;
        updateWordDisplay();
        toggleDefinitionVisibility();
    }

    // 设置按钮点击事件
    private void setupButtonListeners() {
        btnNeverSeen.setOnClickListener(v -> handleWordResponse(0));
        btnForgot.setOnClickListener(v -> handleWordResponse(1));
        btnRemembered.setOnClickListener(v -> handleWordResponse(2));
    }

    // 处理单词响应
    private void handleWordResponse(int response) {
        if (currentIndex >= totalWords) {
            if (reviewList.isEmpty()) {
                // 所有单词都已记住，返回欢迎页面
                returnToWelcomeFragment();
                return;
            } else {
                // 还有需要复习的单词
                wordList = new ArrayList<>(reviewList);
                reviewList.clear();
                currentIndex = 0;
            }
        }

        Word currentWord = wordList.get(currentIndex);
        
        switch (response) {
            case 0: // 没见过
            case 1: // 忘记
                reviewList.add(currentWord);
                break;
            case 2: // 记得
                wordDao.updateRememberedStatus(currentWord.getId(), 1); // 更新单词记住状态
                learningRecordDao.addLearningRecord(currentWord.getId()); // 添加学习记录
                completedWords++;
                break;
        }

        currentIndex++;
        
        if (currentIndex >= totalWords) {
            if (reviewList.isEmpty()) {
                // 所有单词都已记住，返回欢迎页面
                returnToWelcomeFragment();
                return;
            } else {
                // 还有需要复习的单词
                wordList = new ArrayList<>(reviewList);
                reviewList.clear();
                currentIndex = 0;
            }
        }
        
        updateWordDisplay();
    }

    // 更新单词显示
    private void updateWordDisplay() {

        if (currentIndex < totalWords) {
            Word currentWord = wordList.get(currentIndex);
            wordText.setText(currentWord.getWord());
            definitionText.setText(currentWord.getDefinition());
            variantText.setText(currentWord.getVariant());
            topicText.setText(currentWord.getTopic());
            toggleDefinitionVisibility();
            
            // 更新进度显示
            progressText.setText(String.format("已完成：%d/%d", completedWords, totalWords));
        }
    }

    // 返回欢迎页面
    private void returnToWelcomeFragment() {
        btnNeverSeen.setEnabled(false);
        btnForgot.setEnabled(false);
        btnRemembered.setEnabled(false);

        // 创建自定义对话框布局
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_well_done, null);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        LottieAnimationView confettiView = dialogView.findViewById(R.id.confetti_animation);

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
        dialog.setOnDismissListener(dialogInterface -> {
            // 对话框关闭时返回欢迎页面
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_word, new WordRecitationWelcomeFragment())
                    .commit();
        });

        dialog.show();
        
        // 4秒后自动关闭对话框
        dialogView.postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 4000);
    }
} 
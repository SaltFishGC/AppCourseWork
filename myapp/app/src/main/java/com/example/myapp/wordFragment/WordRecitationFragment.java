package com.example.myapp.wordFragment;

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
// todo 制作两个模块的词义遮挡，点击后显示
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_recitation, container, false);
        
        // 初始化视图
        initViews(view);
        
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
    // 加载单词
    private void loadWords() {
        wordList = wordDao.getRandomUnrememberedWords();
        reviewList = new ArrayList<>();
        currentIndex = 0;
        totalWords = wordList.size();
        completedWords = 0;
        updateWordDisplay();
    }

    // 设置按钮点击事件
    private void setupButtonListeners() {
        btnNeverSeen.setOnClickListener(v -> handleWordResponse(0));
        btnForgot.setOnClickListener(v -> handleWordResponse(1));
        btnRemembered.setOnClickListener(v -> handleWordResponse(2));
    }

    // 处理单词响应
    private void handleWordResponse(int response) {
        if (currentIndex >= wordList.size()) {
            if (reviewList.isEmpty()) {
                // 所有单词都已记住，返回欢迎页面
                returnToWelcomeFragment();
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
        
        if (currentIndex >= wordList.size()) {
            if (reviewList.isEmpty()) {
                // 所有单词都已记住，返回欢迎页面
                returnToWelcomeFragment();
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
        if (currentIndex < wordList.size()) {
            Word currentWord = wordList.get(currentIndex);
            wordText.setText(currentWord.getWord());
            definitionText.setText(currentWord.getDefinition());
            variantText.setText(currentWord.getVariant());
            topicText.setText(currentWord.getTopic());
            
            // 更新进度显示
            int remainingWords = totalWords - completedWords;
            progressText.setText(String.format("已完成：%d/%d", completedWords, totalWords));
        }
    }

    // 返回欢迎页面
    private void returnToWelcomeFragment() {
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
                    .replace(R.id.fragment_container, new WordRecitationWelcomeFragment())
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
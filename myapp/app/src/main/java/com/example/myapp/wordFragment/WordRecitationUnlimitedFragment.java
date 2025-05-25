package com.example.myapp.wordFragment;

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

    private WordDao wordDao;
    private WordLearningRecordDao learningRecordDao;
    private Word currentWord;
    private int completedWords;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_recitation_unlimited, container, false);
        
        // 初始化视图
        initViews(view);
        
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

    private void loadNextWord() {
        // 获取一个未记住的单词
        currentWord = wordDao.getRandomUnrememberedWord();
        updateWordDisplay();
    }

    private void setupButtonListeners() {
        btnNeverSeen.setOnClickListener(v -> handleWordResponse(0));
        btnForgot.setOnClickListener(v -> handleWordResponse(1));
        btnRemembered.setOnClickListener(v -> handleWordResponse(2));
        btnEnd.setOnClickListener(v -> returnToWelcomeFragment());
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
        }
    }

    private void updateWordDisplay() {
        if (currentWord != null) {
            wordText.setText(currentWord.getWord());
            definitionText.setText(currentWord.getDefinition());
            variantText.setText(currentWord.getVariant());
            topicText.setText(currentWord.getTopic());
            progressText.setText(String.format("已完成：%d个单词", completedWords));
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
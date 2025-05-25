package com.example.myapp.wordFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapp.R;
import com.example.myapp.dao.SqliteConnection;
import com.example.myapp.dao.WordLearningRecordDao;

import java.util.Date;

public class WordRecitationWelcomeFragment extends Fragment {
    private CalendarView calendarView;
    private TextView todayStats;
    private TextView totalStats;
    private Button startButton;
    private Button unlimitedModeButton;
    private SqliteConnection dbHelper;
    private WordLearningRecordDao learningRecordDao;
    //TODO 利用MaterialCalendarView实现日历显示背诵了几个词
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_recitation_welcome, container, false);

        // 初始化视图
        initViews(view);
        
        // 初始化数据库
        dbHelper = new SqliteConnection(requireContext());
        learningRecordDao = new WordLearningRecordDao(dbHelper);

        // 设置点击事件
        setupClickListeners();
        
        // 显示今天的统计信息
        updateTodayStats();
        
        return view;
    }

    private void initViews(View view) {
        // 初始化视图
        calendarView = view.findViewById(R.id.calendar_view);
        todayStats = view.findViewById(R.id.today_stats);
        totalStats = view.findViewById(R.id.total_stats);
        startButton = view.findViewById(R.id.start_button);
        unlimitedModeButton = view.findViewById(R.id.unlimited_mode_button);
    }

    private void setupClickListeners() {
        // 设置日历点击事件
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            updateStatsForDate(year, month, dayOfMonth);
        });

        // 设置开始按钮点击事件
        startButton.setOnClickListener(v -> {
            // 跳转到背诵页面
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new WordRecitationFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // 设置无限背诵按钮点击事件
        unlimitedModeButton.setOnClickListener(v -> {
            // 跳转到无限背诵页面
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new WordRecitationUnlimitedFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }
    
    private void updateTodayStats() {
        Date today = new Date();
        int todayCount = learningRecordDao.getLearningCountByDate(today);
        todayStats.setText(String.format("今日学习：%d个单词", todayCount));
        
        // 更新总计学习数量
        updateTotalStats();
    }
    
    private void updateStatsForDate(int year, int month, int day) {
        // 创建指定日期的Date对象
        Date selectedDate = new Date(year - 1900, month, day);
        int count = learningRecordDao.getLearningCountByDate(selectedDate);
        todayStats.setText(String.format("%d年%d月%d日学习：%d个单词", 
            year, month + 1, day, count));
    }
    
    private void updateTotalStats() {
        // 获取所有学习记录的数量
        int totalCount = learningRecordDao.getTotalLearningCount();
        totalStats.setText(String.format("总计学习：%d个单词", totalCount));
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次返回欢迎页面时更新统计信息
        updateTodayStats();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 
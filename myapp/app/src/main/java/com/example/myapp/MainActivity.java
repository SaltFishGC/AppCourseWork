package com.example.myapp;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapp.communityFragment.CommunityFragment;
import com.example.myapp.communityFragment.DataSyncFragment;
import com.example.myapp.focusFragment.FocusModeFragment;
import com.example.myapp.focusFragment.FocusModeService;
import com.example.myapp.wordFragment.WordRecitationWelcomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FocusModeService focusModeService;
    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // 如果专注模式正在运行，不允许切换Fragment
            if (focusModeService != null && focusModeService.isRunning()) {
                return false;
            }

            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_word_recitation) {
                selectedFragment = new WordRecitationWelcomeFragment();
            } else if (itemId == R.id.nav_focus_mode) {
                selectedFragment = new FocusModeFragment();
            } else if (itemId == R.id.nav_community) {
                // 检查是否已登录（即 user_id 是否存在）
                if (savedInstanceState == null) {
                    boolean isLoggedIn = checkUserLoginStatus();
                    if (isLoggedIn) {
                        selectedFragment  = new DataSyncFragment();
                    } else {
                        // 否则进入默认登录页面
                        selectedFragment = new CommunityFragment();
                    }
                }

            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
                return true;
            }
            return false;
        });

        // 设置默认选中的Fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_word_recitation);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检查专注模式状态并更新导航栏状态
        updateNavigationBarState();
    }

    private void updateNavigationBarState() {
        if (focusModeService != null && focusModeService.isRunning()) {
            bottomNavigationView.setEnabled(false);
            // 保持当前选中的项
            bottomNavigationView.setSelectedItemId(R.id.nav_focus_mode);
        } else {
            bottomNavigationView.setEnabled(true);
        }
    }

    public void setFocusModeService(FocusModeService service) {
        this.focusModeService = service;
        updateNavigationBarState();
    }
    private boolean checkUserLoginStatus() {
        int userId = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                .getInt("user_id", -1);
        return userId != -1; // 如果 user_id 存在且不为 -1，说明已登录
    }

}
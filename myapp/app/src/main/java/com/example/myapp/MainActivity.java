package com.example.myapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

    // 新增：引用三个Fragment容器
    private FragmentContainerView containerWord, containerFocus, containerCommunity;

    // 新增：保存每个容器对应的Fragment
    private Fragment wordFragment, focusFragment, communityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initFragments();

        setupBottomNavigation();

        showModule(containerWord, wordFragment);

    }
    private void initViews() {
        // 获取底部导航栏
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // 初始化三个Fragment容器
        containerWord = findViewById(R.id.container_word);
        containerFocus = findViewById(R.id.container_focus);
        containerCommunity = findViewById(R.id.container_community);
    }

    private void initFragments() {
        // 预加载三个模块的Fragment
        FragmentManager fm = getSupportFragmentManager();
        wordFragment = fm.findFragmentById(R.id.container_word);
        if (wordFragment == null) {
            wordFragment = new WordRecitationWelcomeFragment();
            fm.beginTransaction()
                    .add(R.id.container_word, wordFragment, "word")
                    .hide(wordFragment)
                    .addToBackStack(null)
                    .commit();
        }
        Log.d("FragmentInit", "initFragments: wordFragment");

        focusFragment = fm.findFragmentById(R.id.container_focus);
        if (focusFragment == null) {
            focusFragment = new FocusModeFragment();
            fm.beginTransaction()
                    .add(R.id.container_focus, focusFragment,  "time")
                    .hide(focusFragment)
                    .addToBackStack(null)
                    .commit();
        }
        Log.d("FragmentInit", "initFragments: focusFragment");

        communityFragment = fm.findFragmentById(R.id.container_community);
        if (communityFragment == null) {
            communityFragment = new CommunityFragment();
            fm.beginTransaction()
                    .add(R.id.container_community, communityFragment)
                    .hide(communityFragment)
                    .addToBackStack(null)
                    .commit();
        }
        Log.d("FragmentInit", "initFragments: communityFragment");
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_word_recitation) {
                showModule(containerWord, new WordRecitationWelcomeFragment());
            } else if (item.getItemId() == R.id.nav_focus_mode) {
                showModule(containerFocus, new FocusModeFragment());
            } else if (item.getItemId() == R.id.nav_community) {
                boolean isLoggedIn = checkUserLoginStatus();
                Fragment fragment = isLoggedIn ? new DataSyncFragment() : new CommunityFragment();
                showModule(containerCommunity, fragment);
            }
            return true;
        });
    }

    // 核心方法：显示指定模块并维护其栈
    private void showModule(FragmentContainerView container, Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();

        // 隐藏所有容器
        for (int id : new int[]{R.id.container_word, R.id.container_focus, R.id.container_community}) {
            Fragment frag = fm.findFragmentById(id);
            View containerView = findViewById(id);
            if (containerView != null) {
                containerView.setVisibility(View.GONE);
            }
        }

        // 显示当前容器
        if (container != null) {
            container.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        // 检查当前可见的容器
        for (int id : new int[]{R.id.container_word, R.id.container_focus, R.id.container_community}) {
            Fragment fragment = fm.findFragmentById(id);
            if (fragment != null && fragment.isVisible()) {
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                    return;
                }
            }
        }

        super.onBackPressed(); // 所有栈都空了才退出应用
    }
}
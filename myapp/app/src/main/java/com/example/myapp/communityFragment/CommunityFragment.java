package com.example.myapp.communityFragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapp.R;
import com.example.myapp.connect.ConnectSet;
import com.example.myapp.dto.MyResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;

public class CommunityFragment extends Fragment {

    private TextInputEditText usernameText;
    private TextInputEditText passwordText;
    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private OkHttpClient client = new OkHttpClient();
    private String serverIp;
    private String serverPort;
    private MaterialButton settingsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        // 初始化控件
        initViews(view);

        // 获取服务器地址和端口
        serverIp=ConnectSet.getServerIp(getContext());
        serverPort=ConnectSet.getServerPort(getContext());

        // 设置监听器
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        usernameText= view.findViewById(R.id.username_input);
        passwordText= view.findViewById(R.id.password_input);
        loginButton= view.findViewById(R.id.btn_login);
        registerButton= view.findViewById(R.id.btn_register);
        settingsButton = view.findViewById(R.id.btn_settings);
    }

    private void setupListeners() {
        setupLoginButton();
        setupRegisterButton();
        settingsButton.setOnClickListener(v -> showServerSettingsDialog());
    }

    private void showServerSettingsDialog() {
        Context context = getContext();
        if (context == null) return;

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_server_setting, null);

        TextInputEditText ipInput = dialogView.findViewById(R.id.server_ip_input);
        TextInputEditText portInput = dialogView.findViewById(R.id.server_port_input);

        ipInput.setText(ConnectSet.getServerIp(context));
        portInput.setText(ConnectSet.getServerPort(context));

        ipInput.setText(ConnectSet.getServerIp(context));
        portInput.setText(ConnectSet.getServerPort(context));

        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("服务器设置")
                .setView(dialogView)
                .setPositiveButton("确认", (dialog, which) -> {
                    String ip = ipInput.getText().toString().trim();
                    String port = portInput.getText().toString().trim();

                    if (ip.isEmpty() || port.isEmpty()) {
                        Toast.makeText(context, "IP 或端口不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 保存配置
                    ConnectSet.setServerIp(context, ip);
                    ConnectSet.setServerPort(context, port);

                    // 可选：更新当前变量
                    serverIp = ip;
                    serverPort = port;

                    Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> {
            String username = usernameText.getText().toString();
            String password = passwordText.getText().toString();
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                return;
            }
            
            loginUser(username, password);
        });
    }

    private void setupRegisterButton() {
        registerButton.setOnClickListener(v -> {
            // 注册按钮逻辑
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new CommunityRegisterFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    private void loginUser(String username, String password) {
        Request request = null;
        try {
            FormBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .build();

            request = new Request.Builder()
                    .url(String.format("http://%s:%s/users/login", serverIp, serverPort))
                    .post(formBody)
                    .build();
        } catch (Exception e) {
            Toast.makeText(getContext(), "登录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    Log.d("NetworkError", e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String json = response.body().string();
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            Gson gson = new Gson();
                            MyResponse<Integer> loginResponse = gson.fromJson(json, new TypeToken<MyResponse<Integer>>(){}.getType());

                            if (loginResponse.isSuccess()) {
                                Integer userId = (Integer) loginResponse.getData();
                                saveUserId(userId);
                                Toast.makeText(getContext(), "登录成功", Toast.LENGTH_SHORT).show();
                                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, new DataSyncFragment());
                                transaction.addToBackStack(null);
                                transaction.commit();
                            } else {
                                Toast.makeText(getContext(), "登录失败: " + loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d("detail", "detali: "+username+" "+password);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "解析响应失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "请求失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void saveUserId(Integer userId) {
        requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                         .edit()
                         .putInt("user_id", userId)
                         .apply();
    }

    private Integer loadUserId() {
        return requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                            .getInt("user_id", -1);
    }
}
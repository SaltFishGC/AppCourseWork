package com.example.myapp.communityFragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapp.R;
import com.example.myapp.connect.ConnectSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommunityRegisterFragment extends Fragment {
    private TextInputEditText usenameText;
    private TextInputEditText passwordText;
    private TextInputEditText passwordAgainText;
    private MaterialButton registerButton;
    private String serverIp;
    private String serverPort;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View  view = inflater.inflate(R.layout.fragment_community_register, container, false);

        serverIp= ConnectSet.getServerIp(getContext());
        serverPort=ConnectSet.getServerPort(getContext());
        usenameText  = view.findViewById(R.id.username_register_input);
        passwordText = view.findViewById(R.id.password_register_input);
        passwordAgainText = view.findViewById(R.id.password_register_again_input);
        registerButton  = view.findViewById(R.id.btn_register_now);

        setUpRegisterButton();

        return view;
    }

    public void setUpRegisterButton(){
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usenameText.getText().toString();
                String password = passwordText.getText().toString();
                String passwordAgain = passwordAgainText.getText().toString();
                if (username.isEmpty() || password.isEmpty() || passwordAgain.isEmpty()) {
                    Toast.makeText(getContext(), "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(passwordAgain)) {
                    Toast.makeText(getContext(), "两次密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    register(username, password);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    public void register(String username, String password) throws JSONException {

        // 构建 JSON 请求体
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("username", username);
        jsonBody.put("password", password);

        RequestBody requestBody = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(String.format("http://%s:%s/users/register", serverIp, serverPort))
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            Gson gson = new Gson();
                            MyResponse<Integer> registerResponse = gson.fromJson(json, new TypeToken<MyResponse<Integer>>() {
                            }.getType());
                            if (registerResponse.isSuccess()) {
                                saveUserId(registerResponse.getData());
                                Toast.makeText(getContext(), "注册成功", Toast.LENGTH_SHORT).show();
                                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, new DataSyncFragment());
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }else {
                                Toast.makeText(getContext(), "注册失败: " + registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "解析失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "请求失败: " + request, Toast.LENGTH_SHORT).show();
                        Log.d("registerFail", "detail: " + username + " " + password);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(getContext(), "注册失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            private void saveUserId(Integer userId) {
                requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                        .edit()
                        .putInt("user_id", userId)
                        .apply();
            }
        });
    }
}
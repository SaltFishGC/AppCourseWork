package com.example.myapp.communityFragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.myapp.R;
import com.example.myapp.adapter.FlexibleDateTypeAdapter;
import com.example.myapp.connect.ConnectSet;
import com.example.myapp.dao.SqliteConnection;
import com.example.myapp.dao.TimeLearnedDao;
import com.example.myapp.dao.WordDao;
import com.example.myapp.dao.WordLearningRecordDao;
import com.example.myapp.dto.MyResponse;
import com.example.myapp.dto.TimeLearnedWithUseridDTO;
import com.example.myapp.dto.WordLearningRecordWithUseridDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import okhttp3.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class DataSyncFragment extends Fragment {
    private Button btnUpload, btnSync, btnQuit;
    private OkHttpClient client = new OkHttpClient();
    private Integer userId;
    private String serverIp;
    private String serverPort;
    private WordLearningRecordDao wordLearningRecordDao;
    private TimeLearnedDao  timeLearnedDao;
    private WordDao wordDao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_sync, container, false);
        // 获取所需信息
        userId = loadUserId();
        serverIp = ConnectSet.getServerIp(requireContext());
        serverPort=ConnectSet.getServerPort(requireContext());

        // 初始化视图
        initViews(view);

        // 初始化数据库和DAO
        SqliteConnection dbHelper = new SqliteConnection(requireContext());
        wordLearningRecordDao = new WordLearningRecordDao(dbHelper);
        timeLearnedDao = new TimeLearnedDao(getContext());
        wordDao = new WordDao(dbHelper);

        // 设置按钮点击事件
        setupListeners();

        return view;
    }

    private void initViews(View view){
        btnUpload = view.findViewById(R.id.btn_upload);
        btnSync = view.findViewById(R.id.btn_sync);
        btnQuit = view.findViewById(R.id.btn_quit);
    }

    private void setupListeners() {
        setupUploadButton();
        setupSyncButton();
        setupQuitButton();
    }

    // 退出按钮
    public void setupQuitButton() {
        btnQuit.setOnClickListener(v -> {
            requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                    .edit()
                    .remove("user_id")  // 或者使用 clear() 清除所有数据
                    .apply();

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_community, new CommunityFragment())
                    .commit();
        });
    }
    // 上传按钮
    private void setupUploadButton() {
        btnUpload.setOnClickListener(v -> {
            uploadDataToServer();
        });
    }
    
    private void uploadDataToServer() {
        // 先删除服务器中的数据
        FormBody formBody = new FormBody.Builder()
                .add("userId", userId.toString())
                .build();
        // 先删除服务器中对应用户的数据
        // 删除单词背诵记录
        Request request = new Request.Builder()
                .url(String.format("http://%s:%s/netem-learned-detail/user/word/delete", serverIp, serverPort))
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "删除数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "数据删除成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "删除失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        // 删除时间学习记录
        request  = new Request.Builder()
                .url(String.format("http://%s:%s/time-learned/user/time/delete", serverIp, serverPort))
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "删除数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "数据删除成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "删除失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // 上传数据以实现覆盖
        // 上传单词背诵记录
//        Gson gson = new GsonBuilder()
//                .setDateFormat("yyyy-MM-dd")
//                .create();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new FlexibleDateTypeAdapter())
                .create();

        List<WordLearningRecordWithUseridDTO> wordLearningRecords = wordLearningRecordDao.getAllLearningRecords(userId);
        String json = gson.toJson(wordLearningRecords);
        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));
        request  = new Request.Builder()
                .url(String.format("http://%s:%s/netem-learned-detail/user/word/save", serverIp, serverPort))
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            String json = response.body().string();
                            Gson  gson = new Gson();
                            MyResponse<Integer> myResponse = gson.fromJson(json, new TypeToken<MyResponse<Integer>>() {}.getType());
                            if (myResponse.isSuccess()) {
                                Toast.makeText(getContext(), "上传成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "上传失败: " + myResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "上传失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "上传数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
        // 上传时间学习记录
        List<TimeLearnedWithUseridDTO> timeLearnedList = timeLearnedDao.getAllTimeLearnedWithUserid(userId);
        String jsonTimeLearnedList = gson.toJson(timeLearnedList);
        Log.d("time test", "uploadDataToServer: "+ jsonTimeLearnedList);
        requestBody  = RequestBody.create(jsonTimeLearnedList, MediaType.parse("application/json"));
        request  = new Request.Builder()
                .url(String.format("http://%s:%s/time-learned/user/time/save", serverIp, serverPort))
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            String json = response.body().string();
                            Gson  gson = new Gson();
                            MyResponse<Object> myResponse = gson.fromJson(json, new TypeToken<MyResponse<Object>>() {}.getType());
                            if (myResponse.isSuccess()) {
                                Toast.makeText(getContext(), "上传成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "上传失败: " + myResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            Toast.makeText(getContext(), "解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getContext(), "上传失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "上传数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

    }

    private void setupSyncButton() {
        btnSync.setOnClickListener(v -> {
            syncDataFromServer();
        });
    }
    
    private void syncDataFromServer() {
        // 这里实现从服务器同步数据的逻辑
        // 同步数据需要先删除掉本地数据库中的数据，然后从服务器获取数据，并保存到数据库中，但为了防止数据没拿到本地的全删了，先尝试获取数据，然后再删除，最后导入
        // 发起网络请求，获取服务器数据
        // 获取尝试获取词汇学习数据
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new FlexibleDateTypeAdapter())
                .create();

        Request request = new Request.Builder()
                .url(String.format("http://%s:%s/netem-learned-detail/user/word/get", serverIp, serverPort))
                .post(new FormBody.Builder()
                        .add("userId", userId.toString())
                        .build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "连接失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        String json = null;
                        try {
                            json = response.body().string();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        try {
                            if (response.body() == null){
                                Toast.makeText(getContext(), "响应体为空 ", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Log.d("word", "data: "+json);
                            MyResponse<List<WordLearningRecordWithUseridDTO>> myResponse = gson.fromJson(json, new TypeToken<MyResponse<List<WordLearningRecordWithUseridDTO>>>() {}.getType());
                            if (myResponse.isSuccess()) {
                                List<WordLearningRecordWithUseridDTO> wordLearningRecords = myResponse.getData();
                                if (wordLearningRecords != null) {
                                    try {
                                        if (wordLearningRecordDao.loadLearningRecords(wordLearningRecords)) {
                                            Toast.makeText(getContext(), "词汇背诵数据同步完成", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(getContext(), "导入数据失败:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    Toast.makeText(getContext(), "获取背诵记录为空", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(getContext(), "同步词汇背诵数据失败: " + myResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            Toast.makeText(getContext(), "解析词汇背诵数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("wordjiexishibai", "reponse: "+json);
                        }
                    } else {
                        Toast.makeText(getContext(), "连接词汇背诵数据失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
        // 获取时间学习数据
        request = new Request.Builder()
                .url(String.format("http://%s:%s/time-learned/user/time/get", serverIp, serverPort))
                .post(new FormBody.Builder()
                        .add("userId", userId.toString())
                        .build())
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                requireActivity().runOnUiThread(()->{
                    if (response.isSuccessful()) { //  请求成功
                        String  json = null;
                        try {
                            json = response.body().string();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            if (response.body() == null){
                                Toast.makeText(getContext(), "响应体为空 ", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            MyResponse<List<TimeLearnedWithUseridDTO>> myResponse = gson.fromJson(json, new TypeToken<MyResponse<List<TimeLearnedWithUseridDTO>>>(){}.getType());
                            Log.d("time learn", "data: "+json);
                            if (myResponse.isSuccess()) { // 获取数据包成功
                                List<TimeLearnedWithUseridDTO> records = myResponse.getData();
                                if (records!=null) { // 数据包非空
                                    if (timeLearnedDao.loadTimeLearned(records)) { // 载入数据包成功
                                        Toast.makeText(getContext(), "同步专注时长成功", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "同步专注时长失败", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    Toast.makeText(getContext(), "获取专注时长记录为空: " + response.message(), Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(getContext(), "获取学习时间失败", Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            Toast.makeText(getContext(), "解析专注时长数据失败: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("timeLearnjiexishibai", "response: " + json);
                        }
                    } else {
                        Toast.makeText(getContext(), "获取专注时长失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "获取失败"+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
        //  更新已背诵
        wordDao.updateRememberedStatus(wordLearningRecordDao.getAllRememberedWordIds());
    }

    private Integer loadUserId() {
        return requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                .getInt("user_id", -1);
    }

}
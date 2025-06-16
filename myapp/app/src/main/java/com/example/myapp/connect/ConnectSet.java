package com.example.myapp.connect;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapp.R;

public class ConnectSet {

    // SharedPreferences 文件名
    private static final String PREF_NAME = "ServerConfig";

    // 键名
    private static final String KEY_SERVER_IP = "server_ip";
    private static final String KEY_SERVER_PORT = "server_port";

    // 获取 SharedPreferences 实例
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 保存服务器 IP 地址
     */
    public static void setServerIp(Context context, String ip) {
        getSharedPreferences(context).edit().putString(KEY_SERVER_IP, ip).apply();
    }

    /**
     * 获取服务器 IP 地址
     */
    public static String getServerIp(Context context) {
        return getSharedPreferences(context).getString(KEY_SERVER_IP, context.getString(R.string.server_ip));
    }

    /**
     * 保存服务器端口号
     */
    public static void setServerPort(Context context, String port) {
        getSharedPreferences(context).edit().putString(KEY_SERVER_PORT, port).apply();
    }

    /**
     * 获取服务器端口号
     */
    public static String getServerPort(Context context) {
        return getSharedPreferences(context).getString(KEY_SERVER_PORT, context.getString(R.string.server_port));
    }

    /**
     * 清除所有保存的服务器配置
     */
    public static void clear(Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }
}

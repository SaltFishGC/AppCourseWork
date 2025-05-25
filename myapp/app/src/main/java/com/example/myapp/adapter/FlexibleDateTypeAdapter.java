package com.example.myapp.adapter;

import android.text.TextUtils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FlexibleDateTypeAdapter extends TypeAdapter<Date> {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    {
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 使用 UTC 时区
    }
    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        out.value(sdf.format(value));
    }

    @Override
    public Date read(JsonReader reader) throws IOException {
        try {
            String value = reader.nextString();
            if (TextUtils.isDigitsOnly(value)) {
                // 如果是纯数字字符串，转为 long 时间戳
                return new Date(Long.parseLong(value));
            } else {
                // 否则按 yyyy-MM-dd 格式解析
                return sdf.parse(value);
            }
        } catch (Exception e) {
            throw new IOException("无法解析日期: " + e.getMessage());
        }
    }
}

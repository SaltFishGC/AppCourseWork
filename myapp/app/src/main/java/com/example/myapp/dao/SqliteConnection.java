package com.example.myapp.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SqliteConnection extends SQLiteOpenHelper {
    private static final String TAG = "SqliteConnection";
    private static final String DATABASE_NAME = "app.db"; // 您的数据库文件名
    private static final int DATABASE_VERSION = 1;
    private final Context context;
    private static String DATABASE_PATH; // 数据库路径，从context上下文获取

    public SqliteConnection(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        if (context != null) {
            DATABASE_PATH = context.getDatabasePath(DATABASE_NAME).getPath();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        importDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 数据库升级时的操作

    }

    public void importDatabase() {
        // 检查数据库是否已经存在
        if (isDatabaseExists()) {
            Log.d(TAG, "数据库已存在，无需导入");
            return;
        }

        try {
            // 获取数据库文件的输入流
            InputStream inputStream = context.getAssets().open(DATABASE_NAME);
            
            // 创建输出流
            OutputStream outputStream = new FileOutputStream(DATABASE_PATH);
            
            // 复制文件
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            // 关闭流
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            
            Log.d(TAG, "数据库导入成功");
        } catch (IOException e) {
            Log.e(TAG, "数据库导入失败", e);
            // 如果导入失败，则创建新的数据库
            SQLiteDatabase db = getWritableDatabase();
            db.close();
        }
    }

    private boolean isDatabaseExists() {
        File dbFile = new File(DATABASE_PATH);
        return dbFile.exists();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        // 确保数据库已导入
        importDatabase();
        return super.getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        // 确保数据库已导入
        importDatabase();
        return super.getWritableDatabase();
    }
}

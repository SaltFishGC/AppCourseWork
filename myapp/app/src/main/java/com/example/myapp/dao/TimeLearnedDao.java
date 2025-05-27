package com.example.myapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapp.dto.TimeLearnedWithUseridDTO;
import com.example.myapp.entity.TimeLearned;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeLearnedDao {
    private SqliteConnection dbHelper;
    private SQLiteDatabase database;
    private static final String TABLE_NAME = "time_learned";
    private static final String COLUMN_TIME = "time_learned";
    private static final String COLUMN_DATE = "time_learned_date";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public TimeLearnedDao(Context context) {
        dbHelper = new SqliteConnection(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // 添加专注记录
    public long insert(TimeLearned timeLearned) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME, timeLearned.getTimeLearned());
        values.put(COLUMN_DATE, timeLearned.getTimeLearnedDate().getTime());
        return database.insert(TABLE_NAME, null, values);
    }

    // 获取指定日期的专注时长
    public double getTimeLearnedByDate(Date date) {
        String dateStr = dateFormat.format(date);
        String[] columns = {COLUMN_TIME};
        String selection = COLUMN_DATE + " = ?";
        String[] selectionArgs = {dateStr};
        
        Cursor cursor = database.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        double totalTime = 0;
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                totalTime += cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TIME));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return totalTime;
    }

    // 获取所有专注记录
    public List<TimeLearned> getAllTimeLearned() {
        List<TimeLearned> timeLearnedList = new ArrayList<>();
        String[] columns = {COLUMN_TIME, COLUMN_DATE};
        
        Cursor cursor = database.query(TABLE_NAME, columns, null, null, null, null, COLUMN_DATE + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                TimeLearned timeLearned = new TimeLearned();
                timeLearned.setTimeLearned(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TIME)));
                try {
                    timeLearned.setTimeLearnedDate(dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                timeLearnedList.add(timeLearned);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return timeLearnedList;
    }

    // 获取总专注时长
    public double getTotalTimeLearned() {
        String[] columns = {COLUMN_TIME};
        Cursor cursor = database.query(TABLE_NAME, columns, null, null, null, null, null);
        double totalTime = 0;
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                totalTime += cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TIME));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return totalTime;
    }

    // 删除指定日期的专注记录
    public int deleteByDate(Date date) {
        String dateStr = dateFormat.format(date);
        String whereClause = COLUMN_DATE + " = ?";
        String[] whereArgs = {dateStr};
        return database.delete(TABLE_NAME, whereClause, whereArgs);
    }

    /**
     * 获取所有专注记录
     * @param userId 用户ID
     */
    public List<TimeLearnedWithUseridDTO> getAllTimeLearnedWithUserid(Integer userId) {
        List<TimeLearnedWithUseridDTO> timeLearnedList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                TimeLearnedWithUseridDTO timeLearned = new TimeLearnedWithUseridDTO();
                timeLearned.setUserId(userId);
                timeLearned.setTimeLearned(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TIME)));
                timeLearned.setTimeLearnedDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE))));
                timeLearnedList.add(timeLearned);
            }
        }
        return timeLearnedList;
    }
    /**
     * 删除所有专注记录
     */
    public void deleteAllTimeLearned() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }
    /**
     * 载入专注记录
     * @param timeLearnedList 专注记录列表
     */
    public boolean loadTimeLearned(List<TimeLearnedWithUseridDTO> timeLearnedList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete(TABLE_NAME, null, null);
        try {
            for (TimeLearnedWithUseridDTO timeLearned : timeLearnedList) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TIME, timeLearned.getTimeLearned());
                values.put(COLUMN_DATE, timeLearned.getTimeLearnedDate().getTime());
                db.insert(TABLE_NAME, null, values);
            }
        }catch (Exception e){
            db.endTransaction();
            return false;
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        return true;
    }
} 
package com.example.myapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.myapp.dto.WordLearningRecordWithUseridDTO;
import com.example.myapp.entity.WordLearningRecord;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class WordLearningRecordDao {
    private final SqliteConnection dbHelper;
    private static final String TABLE_NAME = "netem_learned_detail";

    public WordLearningRecordDao(SqliteConnection dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * 获取指定日期的开始时间戳（0点0分0秒）
     */
    private long getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取指定日期的结束时间戳（23点59分59秒）
     */
    private long getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    /**
     * 添加学习记录
     * @param wordId 单词ID
     * @return 是否添加成功
     */
    public boolean addLearningRecord(int wordId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("netem_learned_id", wordId);
        values.put("netem_learned_date", new Date().getTime()); // 存储当前时间戳

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    /**
     * 获取指定日期的学习记录
     * @param date 日期
     * @return 学习记录列表
     */
    public List<WordLearningRecord> getLearningRecordsByDate(Date date) {
        List<WordLearningRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        long startOfDay = getStartOfDay(date);//获取指定日期的开始时间戳
        long endOfDay = getEndOfDay(date);//获取指定日期的结束时间戳

        String query = "SELECT * FROM " + TABLE_NAME + 
                      " WHERE netem_learned_date >= ? AND netem_learned_date <= ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(startOfDay),
                String.valueOf(endOfDay)
        })) {
            while (cursor.moveToNext()) {
                WordLearningRecord record = new WordLearningRecord();
                record.setNetemLearnedId(cursor.getInt(cursor.getColumnIndexOrThrow("netem_learned_id")));
                record.setNetemLearnedDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow("netem_learned_date"))));
                records.add(record);
            }
        }
        return records;
    }

    /**
     * 获取指定日期的学习单词数量
     * @param date 日期
     * @return 学习单词数量
     */
    public int getLearningCountByDate(Date date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        long startOfDay = getStartOfDay(date);//获取指定日期的开始时间戳
        long endOfDay = getEndOfDay(date);//获取指定日期的结束时间戳
        String query = "SELECT COUNT(DISTINCT netem_learned_id) as count FROM " + TABLE_NAME + 
                      " WHERE netem_learned_date >= ? AND netem_learned_date <= ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(startOfDay),
                String.valueOf(endOfDay)
        })) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            }
        }
        return 0;
    }

    /**
     * 获取总计学习的单词数量
     * @return 总计学习的单词数量
     */
    public int getTotalLearningCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(DISTINCT netem_learned_id) as count FROM " + TABLE_NAME;

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            }
        }
        return 0;
    }

    /**
     * 获取所有单词学习记录
     * @return 单词学习记录列表
     */
    public List<WordLearningRecordWithUseridDTO> getAllLearningRecords(Integer userId) {
        List<WordLearningRecordWithUseridDTO> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                WordLearningRecordWithUseridDTO record = new WordLearningRecordWithUseridDTO();
                record.setUserId(userId);
                record.setNetemLearnedId(cursor.getInt(cursor.getColumnIndexOrThrow("netem_learned_id")));
                record.setNetemLearnedDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow("netem_learned_date"))));
                records.add(record);
            }
        }
        return records;
    }

    /**
     * 删除所有单词学习记录
     */
    public void deleteAllLearningRecords() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    /**
     * 载入单词学习记录
     * @param records 单词学习记录列表
     * @return 是否载入成功
     */
    public boolean loadLearningRecords(List<WordLearningRecordWithUseridDTO> records) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete(TABLE_NAME, null, null);
        try {
            for (WordLearningRecordWithUseridDTO record : records) {
                ContentValues values = new ContentValues();
                values.put("netem_learned_id", record.getNetemLearnedId());
                values.put("netem_learned_date", record.getNetemLearnedDate().getTime());
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
    /**
     * 获取所有已背诵的单词的ID
     */
    public List<Integer> getAllRememberedWordIds() {
        List<Integer> wordIds = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT netem_learned_id FROM " + TABLE_NAME;
        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                wordIds.add(cursor.getInt(cursor.getColumnIndexOrThrow("netem_learned_id")));
            }
        }
        return wordIds;
    }


} 
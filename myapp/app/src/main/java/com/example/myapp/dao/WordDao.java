package com.example.myapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.myapp.entity.Word;
import java.util.ArrayList;
import java.util.List;

public class WordDao {
    private final SqliteConnection dbHelper;
    private static final String TABLE_NAME = "netem_full_list_user_remembered";

    public WordDao(SqliteConnection dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * 从未记住的单词中随机获取10个单词
     * @return 单词列表
     */
    public List<Word> getRandomUnrememberedWords() {
        List<Word> words = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // SQL查询：从未记住的单词中随机选择10个
        String query = "SELECT * FROM " + TABLE_NAME + 
                      " WHERE remembered = 0 " +
                      " ORDER BY RANDOM() " +
                      " LIMIT 10";

        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                Word word = new Word();
                word.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                word.setFrequency(cursor.getInt(cursor.getColumnIndexOrThrow("frequency")));
                word.setWord(cursor.getString(cursor.getColumnIndexOrThrow("word")));
                word.setDefinition(cursor.getString(cursor.getColumnIndexOrThrow("definition")));
                word.setVariant(cursor.getString(cursor.getColumnIndexOrThrow("variant")));
                word.setTopic(cursor.getString(cursor.getColumnIndexOrThrow("topic")));
                word.setRemembered(cursor.getInt(cursor.getColumnIndexOrThrow("remembered")));
                words.add(word);
            }
        }
        return words;
    }

    /**
     * 从未记住的单词中随机获取1个单词
     * @return 单词列
     */
    public Word getRandomUnrememberedWord() {
        Word word = new Word();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // SQL查询：从未记住的单词中随机选择1个
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE remembered = 0 " +
                " ORDER BY RANDOM() " +
                " LIMIT 1";

        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                word.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                word.setFrequency(cursor.getInt(cursor.getColumnIndexOrThrow("frequency")));
                word.setWord(cursor.getString(cursor.getColumnIndexOrThrow("word")));
                word.setDefinition(cursor.getString(cursor.getColumnIndexOrThrow("definition")));
                word.setVariant(cursor.getString(cursor.getColumnIndexOrThrow("variant")));
                word.setTopic(cursor.getString(cursor.getColumnIndexOrThrow("topic")));
                word.setRemembered(cursor.getInt(cursor.getColumnIndexOrThrow("remembered")));
            }
        }
        return word;
    }

    /**
     * 更新单词的记住状态
     * @param wordId 单词ID
     * @param remembered 记住状态（1表示已记住，0表示未记住）
     * @return 是否更新成功
     */
    public boolean updateRememberedStatus(int wordId, int remembered) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("remembered", remembered);

        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(wordId)};

        int rowsAffected = db.update(TABLE_NAME, values, whereClause, whereArgs);
        return rowsAffected > 0;
    }
    /**
     * 根据输入integer数组更新单词的记住状态
     * @param wordIds integer数组
     */
    public void updateRememberedStatus(List<Integer> wordIds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // 先删除所有单词的记住状态
        ContentValues value = new ContentValues();
        value.put("remembered", 0);
        db.update(TABLE_NAME,  value, null, null);
        for (Integer wordId : wordIds) {
            ContentValues values = new ContentValues();
            values.put("remembered", 1);

            String whereClause = "id = ?";
            String[] whereArgs = {String.valueOf(wordId)};

            db.update(TABLE_NAME, values, whereClause, whereArgs);
        }
    }
    /**
     * 根据id获取单词
     * @param wordId 单词ID
     * @return 单词对象
     */
    public Word getWordById(int wordId) {
        SQLiteDatabase  db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(wordId)})) {
            if (cursor.moveToFirst()) {
                Word word = new Word();
                word.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                word.setFrequency(cursor.getInt(cursor.getColumnIndexOrThrow("frequency")));
                word.setWord(cursor.getString(cursor.getColumnIndexOrThrow("word")));
                word.setDefinition(cursor.getString(cursor.getColumnIndexOrThrow("definition")));
                word.setVariant(cursor.getString(cursor.getColumnIndexOrThrow("variant")));
                word.setTopic(cursor.getString(cursor.getColumnIndexOrThrow("topic")));
                word.setRemembered(cursor.getInt(cursor.getColumnIndexOrThrow("remembered")));
                return word;
            }
        }
        return null;
    }
}

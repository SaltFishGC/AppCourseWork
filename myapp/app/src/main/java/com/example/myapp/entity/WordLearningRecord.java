package com.example.myapp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class WordLearningRecord {
    private Integer netemLearnedId;  // 对应数据库中的netem_learned_id

    private Date netemLearnedDate;  // 对应数据库中的netem_learned_date

    public WordLearningRecord() {
    }

    public WordLearningRecord(int netemLearnedId, Date netemLearnedDate) {
        this.netemLearnedId = netemLearnedId;
        this.netemLearnedDate = netemLearnedDate;
    }

    public int getNetemLearnedId() {
        return netemLearnedId;
    }

    public void setNetemLearnedId(int netemLearnedId) {
        this.netemLearnedId = netemLearnedId;
    }

    public Date getNetemLearnedDate() {
        return netemLearnedDate;
    }

    public void setNetemLearnedDate(Date netemLearnedDate) {
        this.netemLearnedDate = netemLearnedDate;
    }
} 
package com.example.myapp.dto;

import com.example.myapp.entity.WordLearningRecord;

public class WordLearningRecordWithUseridDTO extends WordLearningRecord {
    private Integer userId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}

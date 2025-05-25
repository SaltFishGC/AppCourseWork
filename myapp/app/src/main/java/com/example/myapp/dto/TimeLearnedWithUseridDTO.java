package com.example.myapp.dto;

import com.example.myapp.entity.TimeLearned;

public class TimeLearnedWithUseridDTO extends TimeLearned {
    private Integer userId;

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }
}

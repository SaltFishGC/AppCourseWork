package com.example.myapp.entity;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class TimeLearned {
    private Double timeLearned; // 专注时长（分钟）

    private Date timeLearnedDate; // 专注日期

    public TimeLearned() {
    }

    public TimeLearned(double timeLearned, Date timeLearnedDate) {
        this.timeLearned = timeLearned;
        this.timeLearnedDate = timeLearnedDate;
    }

    public double getTimeLearned() {
        return timeLearned;
    }

    public void setTimeLearned(double timeLearned) {
        this.timeLearned = timeLearned;
    }

    public Date getTimeLearnedDate() {
        return timeLearnedDate;
    }

    public void setTimeLearnedDate(Date timeLearnedDate) {
        this.timeLearnedDate = timeLearnedDate;
    }
} 
package com.example.myapp.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    // 管理数据同步完成事件
    private final MutableLiveData<Boolean> syncCompletedEvent = new MutableLiveData<>();

    public void notifySyncCompleted() {
        syncCompletedEvent.postValue(true); // 可重复发送事件
    }

    public LiveData<Boolean> getSyncCompletedEvent() {
        return syncCompletedEvent;
    }

    // 新增方法：用于手动重置事件状态
    public void resetSyncEvent() {
        syncCompletedEvent.postValue(false);
    }
}

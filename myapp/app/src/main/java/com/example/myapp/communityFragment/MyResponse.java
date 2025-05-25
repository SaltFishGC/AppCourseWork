package com.example.myapp.communityFragment;

public class MyResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public boolean isSuccess() {
        return code == 200;
    }

    public Integer getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
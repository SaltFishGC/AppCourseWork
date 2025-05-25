package com.myapp.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class response {
    private Integer code;
    private String message;
    private Object data;
    public static response ok() {
        response response = new response();
        response.setCode(200);
        response.setMessage("ok");
        return response;
    }
    public static response ok(Object data){
        response response = new response();
        response.setCode(200);
        response.setMessage("ok");
        response.setData(data);
        return response;
    }
    public static response error(String message){
        response response = new response();
        response.setCode(400);
        response.setMessage(message);
        return response;
    }
}

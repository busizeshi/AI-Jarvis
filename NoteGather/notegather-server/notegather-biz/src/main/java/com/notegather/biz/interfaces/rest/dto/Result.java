package com.notegather.biz.interfaces.rest.dto;

import lombok.Data;

/**
 * 统一响应结果
 * 
 * @author NoteGather
 * @since 1.0.0
 */
@Data
public class Result<T> {
    
    private Integer code;
    private String message;
    private T data;
    
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        return result;
    }
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMessage(message);
        return result;
    }
}

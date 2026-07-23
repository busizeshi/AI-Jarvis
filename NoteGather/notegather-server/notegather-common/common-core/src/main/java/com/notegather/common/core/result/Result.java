package com.notegather.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 业务状态码
     */
    private String code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 链路追踪 ID
     */
    private String traceId;
    
    public static <T> Result<T> ok() {
        return new Result<>("OK", "success", null, null);
    }
    
    public static <T> Result<T> ok(T data) {
        return new Result<>("OK", "success", data, null);
    }
    
    public static <T> Result<T> success() {
        return ok();
    }
    
    public static <T> Result<T> success(T data) {
        return ok(data);
    }
    
    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(code, message, null, null);
    }
    
    public static <T> Result<T> fail(String message) {
        return new Result<>("ERROR", message, null, null);
    }
}

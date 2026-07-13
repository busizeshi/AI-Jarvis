package com.notegather.common.core.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serializable;

/**
 * 统一响应体
 *
 * @param <T> 数据类型
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private final int code;
    private final String message;
    private T data;

    private Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ==================== 成功响应 ====================

    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ==================== 失败响应 ====================

    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.INTERNAL_ERROR.getCode(), ResultCode.INTERNAL_ERROR.getMessage());
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.INTERNAL_ERROR.getCode(), message);
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage());
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message);
    }

    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message);
    }

    // ==================== 工具方法 ====================

    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}

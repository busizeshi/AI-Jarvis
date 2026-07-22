package com.notegather.common.core.exception;

import lombok.Getter;

/**
 * 业务异常基类
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final String code;
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(String message) {
        this("BUSINESS_ERROR", message);
    }
}

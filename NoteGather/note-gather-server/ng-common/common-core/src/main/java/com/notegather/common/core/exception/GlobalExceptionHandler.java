package com.notegather.common.core.exception;

import com.notegather.common.core.result.Result;
import com.notegather.common.core.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("[BusinessException] uri={} code={} msg={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return ResponseEntity.status(resolveHttpStatus(e.getCode())).body(Result.fail(e.getCode(), e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[ParamError] {}", message);
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining("; "));
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("[UnexpectedError] uri={}", request.getRequestURI(), e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }

    private HttpStatus resolveHttpStatus(int code) {
        if (code >= 400 && code <= 599) {
            return HttpStatus.valueOf(code);
        }
        return switch (code) {
            case 1002 -> HttpStatus.CONFLICT;
            case 1003, 1005, 1006, 1007, 1008 -> HttpStatus.UNAUTHORIZED;
            case 1004 -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}

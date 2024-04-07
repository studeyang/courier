package com.github.open.courier.admin.vo;

import lombok.Value;

/**
 * @author yanglulu
 */
@Value
public class Result<T> {

    private static final int SUCCESS_CODE = 200;
    private static final int FAIL_CODE = 500;

    int code;
    String msg;
    T content;

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, null, null);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>(SUCCESS_CODE, message, null);
    }

    public static <T> Result<T> success(String message, T content) {
        return new Result<>(SUCCESS_CODE, message, content);
    }

    public static <T> Result<T> fail() {
        return new Result<>(FAIL_CODE, null, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(FAIL_CODE, message, null);
    }

    public static <T> Result<T> fail(String message, T content) {
        return new Result<>(FAIL_CODE, message, content);
    }
}


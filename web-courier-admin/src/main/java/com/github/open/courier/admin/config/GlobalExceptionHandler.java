package com.github.open.courier.admin.config;

import com.github.open.courier.admin.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author yanglulu
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    private Result<Object> exceptionHandler(Exception exception) {
        log.error(exception.getMessage(), exception);
        return Result.fail(exception.getMessage());
    }

}

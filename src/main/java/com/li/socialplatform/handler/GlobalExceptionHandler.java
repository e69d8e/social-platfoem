package com.li.socialplatform.handler;

import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.exception.BizException;
import com.li.socialplatform.common.exception.MethodArgumentNotValidException;
import com.li.socialplatform.pojo.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author e69d8e
 * @since 2025/12/8 16:03
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler  {
    // 处理业务逻辑异常
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result> handleBizException(BizException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(e.getCode(), e.getMessage()));
    }
    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(e.getCode(), e.getMessage()));
    }
    // 处理所有未捕获的异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception e) {
        log.error("服务器异常：{}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(MessageConstant.EXCEPTION));
    }
}

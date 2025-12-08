package com.li.socialplatform.common.exception;

import com.li.socialplatform.common.constant.MessageConstant;
import lombok.Getter;

/**
 * @author e69d8e
 * @since 2025/12/8 16:13
 */
@Getter
public class MethodArgumentNotValidException extends RuntimeException{
    private final String code;

    public MethodArgumentNotValidException(String message) {
        super(message);
        this.code = MessageConstant.MethodArgumentNotValidException_CODE;
    }
}

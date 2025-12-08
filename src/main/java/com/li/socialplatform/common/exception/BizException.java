package com.li.socialplatform.common.exception;

import com.li.socialplatform.common.constant.MessageConstant;
import lombok.Getter;

/**
 * @author e69d8e
 * @since 2025/12/8 16:08
 */
@Getter
public class BizException extends RuntimeException{
    private final String code;

    public BizException(String message) {
        super(message);
        this.code = MessageConstant.BizException_CODE;
    }

}

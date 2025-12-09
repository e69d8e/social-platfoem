package com.li.socialplatform.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author e69d8e
 * @since 2025/12/8 14:57
 */
@Data
@AllArgsConstructor
public class Result {
    private String code; // 状态码 1 成功 0 失败
    private String message;
    private Object data;
    private Long total;

    public static Result ok() {
        return new Result("1", "success", "", 0L);
    }
    public static Result ok(Object data) {
        return new Result("1", "success", data, 0L);
    }
    public static Result ok(Object data, Long total) {
        return new Result("1", "success", data, total);
    }
    public static Result error(String message) {
        return new Result("0", message, "", 0L);
    }
    public static Result error() {
        return new Result("0", "error", "", 0L);
    }
    public static Result error(String message, Object data) {
        return new Result("0", message, data, 0L);
    }
    public static Result error(String message, String code) {
        return new Result(code, message, "", 0L);
    }
}

package com.li.socialplatform.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的限流注解，使用滑动窗口算法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流维度
     */
    LimitType limitType() default LimitType.USER;

    /**
     * 时间窗口大小
     */
    int timeWindow() default 60;

    /**
     * 时间窗口单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 时间窗口内允许的最大请求数
     */
    int maxRequests() default 10;

    /**
     * 限流维度枚举
     */
    enum LimitType {
        /**
         * 按用户限流，基于当前登录用户ID
         */
        USER,
        /**
         * 按IP限流
         */
        IP,
        /**
         * 全局限流，不区分用户和IP
         */
        GLOBAL
    }
}

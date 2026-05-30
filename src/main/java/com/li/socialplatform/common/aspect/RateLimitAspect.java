package com.li.socialplatform.common.aspect;

import com.li.socialplatform.common.annotation.RateLimit;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis ZSet 滑动窗口的限流切面
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);
        long windowSeconds = rateLimit.timeUnit().toSeconds(rateLimit.timeWindow());
        int maxRequests = rateLimit.maxRequests();

        long now = System.currentTimeMillis();
        long windowStart = now - windowSeconds * 1000;

        // 移除窗口外的过期数据
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // 统计当前窗口内的请求数
        Long count = redisTemplate.opsForZSet().size(key);
        if (count != null && count >= maxRequests) {
            throw new BizException(MessageConstant.RATE_LIMIT_EXCEEDED);
        }

        // 记录本次请求（时间戳 + UUID 作为 member，避免同毫秒碰撞）
        String member = now + ":" + UUID.randomUUID().toString().substring(0, 8);
        redisTemplate.opsForZSet().add(key, member, now);
        redisTemplate.expire(key, windowSeconds * 2, TimeUnit.SECONDS);

        return joinPoint.proceed();
    }

    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String suffix = className + ":" + methodName;

        String identifier = switch (rateLimit.limitType()) {
            case IP -> getClientIp();
            case USER -> getCurrentUserId();
            case GLOBAL -> "global";
        };

        return KeyConstant.RATE_LIMIT_KEY + suffix + ":" + identifier;
    }

    private String getClientIp() {
        HttpServletRequest request = getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    private String getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String name = auth.getName();
                if (name != null && !name.equals("anonymousUser")) {
                    return name;
                }
            }
        } catch (Exception ignored) {
            // fallback
        }
        return "anonymous";
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest();
    }
}

package com.li.socialplatform.common.utils;

import com.li.socialplatform.common.constant.KeyConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author e69d8e
 * @since 2026/05/23 12:54
 */
@Component
@RequiredArgsConstructor
public class UserIntersetScoreUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public void changeScore(long userId, int categoryId, int score) {
        Object value = redisTemplate.opsForHash().get(KeyConstant.USER_INTEREST_SCORE_KEY + userId, String.valueOf(categoryId));
        int s = 0;
        if (value instanceof Number) {
            s = ((Number) value).intValue();
        }
//        Integer s = (Integer) redisTemplate.opsForHash().get(KeyConstant.USER_INTERESTS + userId, categoryId);
//        s = s == null ? 0 : s;
        redisTemplate.opsForHash().put(KeyConstant.USER_INTEREST_SCORE_KEY + userId, String.valueOf(categoryId), s + score);
    }
}

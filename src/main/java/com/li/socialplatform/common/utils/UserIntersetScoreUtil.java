package com.li.socialplatform.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.pojo.entity.UserInterestScore;
import com.li.socialplatform.server.mapper.UserInterestScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author e69d8e
 * @since 2026/05/23 12:54
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserIntersetScoreUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserInterestScoreMapper userInterestScoreMapper;
    private final AsyncTaskUtil asyncTaskUtil;

    private static final long INTEREST_SCORE_TTL_DAYS = 7;

    public void changeScore(long userId, int categoryId, int score) {
        String key = KeyConstant.USER_INTEREST_SCORE_KEY + userId;
        String field = String.valueOf(categoryId);

        // Redis 原子自增
        redisTemplate.opsForHash().increment(key, field, score);
        // 刷新过期时间
        redisTemplate.expire(key, INTEREST_SCORE_TTL_DAYS, TimeUnit.DAYS);

        // 读取最新值，异步持久化到 DB
        Object newValue = redisTemplate.opsForHash().get(key, field);
        int totalScore = newValue instanceof Number ? ((Number) newValue).intValue() : 0;
        asyncTaskUtil.asyncSaveUserInterestScore(userId, categoryId, totalScore);
    }

    public Map<Integer, Integer> getUserInterestScores(Long userId) {
        String key = KeyConstant.USER_INTEREST_SCORE_KEY + userId;

        // Redis 缓存未命中，从 DB 加载
        if (!redisTemplate.hasKey(key)) {
            return loadScoresFromDB(userId);
        }

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Map<Integer, Integer> scores = new HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            int categoryId = Integer.parseInt(entry.getKey().toString());
            int value = entry.getValue() instanceof Number ? ((Number) entry.getValue()).intValue() : 0;
            scores.put(categoryId, value);
        }
        return scores;
    }

    private Map<Integer, Integer> loadScoresFromDB(Long userId) {
        String key = KeyConstant.USER_INTEREST_SCORE_KEY + userId;

        List<UserInterestScore> dbScores = userInterestScoreMapper.selectList(
                new LambdaQueryWrapper<UserInterestScore>().eq(UserInterestScore::getUserId, userId));

        Map<Integer, Integer> scores = new HashMap<>();
        if (dbScores.isEmpty()) {
            return scores;
        }

        Map<String, Object> hashMap = new HashMap<>();
        for (UserInterestScore us : dbScores) {
            scores.put(us.getCategoryId(), us.getScore());
            hashMap.put(String.valueOf(us.getCategoryId()), us.getScore());
        }

        // 回填 Redis 并设置过期
        redisTemplate.opsForHash().putAll(key, hashMap);
        redisTemplate.expire(key, INTEREST_SCORE_TTL_DAYS, TimeUnit.DAYS);
        log.debug("从DB加载用户 {} 兴趣分到Redis, 共 {} 条", userId, dbScores.size());

        return scores;
    }
}

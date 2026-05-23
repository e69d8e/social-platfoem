package com.li.socialplatform.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.pojo.entity.BanRecord;
import com.li.socialplatform.server.mapper.BanRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author e69d8e
 * @since 2026/05/23
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BanCacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final BanRecordMapper banRecordMapper;

    private static final long BAN_CACHE_TTL_DAYS = 7;

    // ==================== 用户封禁 ====================

    public void addBanUser(Long adminId, Long targetUserId) {
        banRecordMapper.insert(new BanRecord(null, adminId, targetUserId, 0, null));
        redisTemplate.opsForZSet().add(KeyConstant.BAN_USER_KEY, targetUserId, System.currentTimeMillis());
        redisTemplate.expire(KeyConstant.BAN_USER_KEY, BAN_CACHE_TTL_DAYS, TimeUnit.DAYS);
    }

    public void removeBanUser(Long targetUserId) {
        banRecordMapper.delete(new LambdaQueryWrapper<BanRecord>()
                .eq(BanRecord::getTargetId, targetUserId)
                .eq(BanRecord::getType, 0));
        redisTemplate.opsForZSet().remove(KeyConstant.BAN_USER_KEY, targetUserId);
    }

    public Long getBanUserTotal() {
        Long size = redisTemplate.opsForZSet().size(KeyConstant.BAN_USER_KEY);
        if (size != null && size > 0) {
            return size;
        }
        loadBanUsersFromDB();
        Long reloaded = redisTemplate.opsForZSet().size(KeyConstant.BAN_USER_KEY);
        return reloaded != null ? reloaded : 0L;
    }

    public Set<Object> getBanUserIds(long start, long end) {
        Set<Object> members = redisTemplate.opsForZSet().range(KeyConstant.BAN_USER_KEY, start, end);
        if (members != null && !members.isEmpty()) {
            return members;
        }
        loadBanUsersFromDB();
        return redisTemplate.opsForZSet().range(KeyConstant.BAN_USER_KEY, start, end);
    }

    private void loadBanUsersFromDB() {
        List<BanRecord> records = banRecordMapper.selectList(
                new LambdaQueryWrapper<BanRecord>().eq(BanRecord::getType, 0));
        if (records.isEmpty()) {
            return;
        }
        for (BanRecord record : records) {
            double score = record.getCreateTime() != null
                    ? record.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : (double) System.currentTimeMillis();
            redisTemplate.opsForZSet().add(KeyConstant.BAN_USER_KEY, record.getTargetId(), score);
        }
        redisTemplate.expire(KeyConstant.BAN_USER_KEY, BAN_CACHE_TTL_DAYS, TimeUnit.DAYS);
        log.debug("从DB加载封禁用户数据: {} 条", records.size());
    }

    // ==================== 帖子封禁 ====================

    public void addBanPost(Long reviewerId, Long postId) {
        banRecordMapper.insert(new BanRecord(null, reviewerId, postId, 1, null));
        String key = KeyConstant.BAN_POST_KEY + reviewerId;
        redisTemplate.opsForZSet().add(key, postId, System.currentTimeMillis());
        redisTemplate.expire(key, BAN_CACHE_TTL_DAYS, TimeUnit.DAYS);
    }

    public void removeBanPost(Long reviewerId, Long postId) {
        banRecordMapper.delete(new LambdaQueryWrapper<BanRecord>()
                .eq(BanRecord::getUserId, reviewerId)
                .eq(BanRecord::getTargetId, postId)
                .eq(BanRecord::getType, 1));
        redisTemplate.opsForZSet().remove(KeyConstant.BAN_POST_KEY + reviewerId, postId);
    }

    public Long getBanPostTotal(Long reviewerId) {
        String key = KeyConstant.BAN_POST_KEY + reviewerId;
        Long size = redisTemplate.opsForZSet().size(key);
        if (size != null && size > 0) {
            return size;
        }
        loadBanPostsFromDB(reviewerId);
        Long reloaded = redisTemplate.opsForZSet().size(key);
        return reloaded != null ? reloaded : 0L;
    }

    public Set<Object> getBanPostIds(Long reviewerId, long start, long end) {
        String key = KeyConstant.BAN_POST_KEY + reviewerId;
        Set<Object> members = redisTemplate.opsForZSet().range(key, start, end);
        if (members != null && !members.isEmpty()) {
            return members;
        }
        loadBanPostsFromDB(reviewerId);
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    private void loadBanPostsFromDB(Long reviewerId) {
        String key = KeyConstant.BAN_POST_KEY + reviewerId;
        List<BanRecord> records = banRecordMapper.selectList(
                new LambdaQueryWrapper<BanRecord>()
                        .eq(BanRecord::getUserId, reviewerId)
                        .eq(BanRecord::getType, 1));
        if (records.isEmpty()) {
            return;
        }
        for (BanRecord record : records) {
            double score = record.getCreateTime() != null
                    ? record.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : (double) System.currentTimeMillis();
            redisTemplate.opsForZSet().add(key, record.getTargetId(), score);
        }
        redisTemplate.expire(key, BAN_CACHE_TTL_DAYS, TimeUnit.DAYS);
        log.debug("从DB加载审查员 {} 封禁帖子数据: {} 条", reviewerId, records.size());
    }
}

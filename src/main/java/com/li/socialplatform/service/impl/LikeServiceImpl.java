package com.li.socialplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.utils.UserIdUtil;
import com.li.socialplatform.mapper.LikeMapper;
import com.li.socialplatform.pojo.entity.LikeRecord;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.ILikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author e69d8e
 * @since 2025/12/9 21:30
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl extends ServiceImpl<LikeMapper, LikeRecord> implements ILikeService {

    private final LikeMapper likeMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserIdUtil userIdUtil;

    @Override
    public Result like(Long postId) {
        Long userId = userIdUtil.getUserId();
        String key = KeyConstant.LIKE_KEY + postId;
        Boolean member = redisTemplate.opsForSet().isMember(key, userId);
        if (Boolean.TRUE.equals(member)) {
            // 点赞数-1
            redisTemplate.opsForValue().increment(KeyConstant.LIKE_COUNT + postId, -1);
            likeMapper.delete(new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getPostId, postId).eq(LikeRecord::getUserId, userId));
            redisTemplate.opsForSet().remove(key, userId);
        } else {
            // 点赞数+1
            redisTemplate.opsForValue().increment(KeyConstant.LIKE_COUNT + postId, 1);
            likeMapper.delete(new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getPostId, postId).eq(LikeRecord::getUserId, userId));
            redisTemplate.opsForSet().add(key, userId);
        }
        return Result.ok("点赞/取消成功", "");
    }
}
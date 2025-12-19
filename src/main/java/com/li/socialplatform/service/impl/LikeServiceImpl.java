package com.li.socialplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.utils.UserIdUtil;
import com.li.socialplatform.mapper.LikeMapper;
import com.li.socialplatform.pojo.entity.LikeRecord;
import com.li.socialplatform.pojo.entity.Post;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.ILikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author e69d8e
 * @since 2025/12/9 21:30
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl extends ServiceImpl<LikeMapper, LikeRecord> implements ILikeService {

    private final LikeMapper likeMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserIdUtil userIdUtil;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public Result like(Long postId) {
        Long userId = userIdUtil.getUserId();
        String key = KeyConstant.LIKE_KEY + postId;
        Boolean member = redisTemplate.opsForSet().isMember(key, userId);
        if (Boolean.TRUE.equals(member)) {
            // 点赞数-1
            Long increment = redisTemplate.opsForValue().increment(KeyConstant.LIKE_COUNT + postId, -1);
            likeMapper.delete(new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getPostId, postId).eq(LikeRecord::getUserId, userId));
            redisTemplate.opsForSet().remove(key, userId);
            // 更新 Elasticsearch
            Post post = elasticsearchOperations.get(postId.toString(), Post.class);
            if (post != null) {
                if (increment != null) {
                    post.setCount(increment.intValue());
                    elasticsearchOperations.save(post);
                }
            }
        } else {
            // 点赞数+1
            Long increment = redisTemplate.opsForValue().increment(KeyConstant.LIKE_COUNT + postId, 1);
            likeMapper.delete(new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getPostId, postId).eq(LikeRecord::getUserId, userId));
            redisTemplate.opsForSet().add(key, userId);
            // 更新 Elasticsearch
            Post post = elasticsearchOperations.get(postId.toString(), Post.class);
            if (post != null) {
                if (increment != null) {
                    post.setCount(increment.intValue());
                    elasticsearchOperations.save(post);
                }
            }
        }
        return Result.ok(MessageConstant.LIKE_SUCCESS, "");
    }
}
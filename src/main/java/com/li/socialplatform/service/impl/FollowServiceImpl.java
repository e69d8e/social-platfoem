package com.li.socialplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.exception.MethodArgumentNotValidException;
import com.li.socialplatform.mapper.FollowMapper;
import com.li.socialplatform.mapper.UserMapper;
import com.li.socialplatform.pojo.entity.Follow;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.pojo.entity.User;
import com.li.socialplatform.service.IFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author e69d8e
 * @since 2025/12/8 23:02
 */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    private final FollowMapper followMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserMapper userMapper;

    // 获取当前登录用户的用户名
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
    @Override
    public Result follow(Long id) {
        if (id == null) {
            throw new MethodArgumentNotValidException(MessageConstant.ID_IS_NULL);
        }
        // 获取当前用户id
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername()));
        if (Objects.equals(user.getId(), id)) {
            return Result.error(MessageConstant.USER_CANNOT_FOLLOW_SELF);
        }
        // 判断用户是否存在
        if (userMapper.selectById(id) == null){
            return Result.error(MessageConstant.USER_NOT_EXIST);
        }
        Follow follow = followMapper.selectOne(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFolloweeId, id).eq(Follow::getFollowerId, user.getId()));
        if (follow != null) {
            return Result.error(MessageConstant.USER_IS_FOLLOWED);
        }
        // 粉丝数加一
        redisTemplate.opsForValue().increment(KeyConstant.FOLLOW_COUNT_KEY + id, 1);
        // 缓存关注列表
        redisTemplate.opsForSet().add(KeyConstant.FOLLOW_LIST_KEY + user.getId(), id);
        // 添加关注
        followMapper.insert(new Follow(null, user.getId(), id, null));
        return Result.ok();
    }

    @Override
    public Result cancelFollow(Long id) {
        if (id == null) {
            throw new MethodArgumentNotValidException(MessageConstant.ID_IS_NULL);
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername()));
        int delete = followMapper.delete(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFolloweeId, id)
                        .eq(Follow::getFollowerId, user.getId()));
        if (delete == 0) {
            return Result.error(MessageConstant.USER_NOT_FOLLOWED);
        }
        redisTemplate.opsForValue().increment(KeyConstant.FOLLOW_COUNT_KEY + id, -1);
        return Result.ok();
    }
}

package com.li.socialplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.exception.MethodArgumentNotValidException;
import com.li.socialplatform.mapper.AuthorityMapper;
import com.li.socialplatform.mapper.FollowMapper;
import com.li.socialplatform.mapper.UserMapper;
import com.li.socialplatform.pojo.entity.Follow;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.pojo.entity.User;
import com.li.socialplatform.pojo.vo.UserVO;
import com.li.socialplatform.service.IFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author e69d8e
 * @since 2025/12/8 23:02
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    private final FollowMapper followMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserMapper userMapper;
    private final AuthorityMapper authorityMapper;

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
        // 缓存粉丝列表
        redisTemplate.opsForSet().add(KeyConstant.FANS_LIST_KEY + id, user.getId());
        // 缓存关注列表
        redisTemplate.opsForSet().add(KeyConstant.FOLLOW_LIST + user.getId(), id);
        // 添加关注
        followMapper.insert(new Follow(null, user.getId(), id, null));
        // 将要关注用户的帖子查找出来
        Set<ZSetOperations.TypedTuple<Object>> typedTuples =
                redisTemplate.opsForZSet().rangeWithScores(KeyConstant.POST_KEY + id, 0, -1);
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok("关注成功", "");
        }
        // 解析
        // 获取 id
        List<Long> ids = typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getValue).map(String::valueOf).map(Long::valueOf).toList();
        // 获取 score
        List<Double> scores = typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getScore).toList();
        // 关注后要将该用户的所有帖子推送到我的关注
        String key = KeyConstant.POST_LIST_KEY + user.getId();
        for (int i = 0; i < ids.size(); i++) {
            redisTemplate.opsForZSet().add(key, ids.get(i), scores.get(i));
        }
        return Result.ok("关注成功", "");
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
            return Result.error(MessageConstant.USER_NOT_FOLLOWED, List.of());
        }
        // 获取当前用户id
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername()));
        if (Objects.equals(u.getId(), id)) {
            return Result.error(MessageConstant.USER_CANNOT_FOLLOW_SELF);
        }
        // 粉丝数减一
        redisTemplate.opsForSet().remove(KeyConstant.FANS_LIST_KEY + id, user.getId());
        redisTemplate.opsForValue().increment(KeyConstant.FOLLOW_COUNT_KEY + id, -1);
        redisTemplate.opsForSet().remove(KeyConstant.FOLLOW_LIST + u.getId(), id);
        // 将要取关的用户的帖子查找出来
        Set<ZSetOperations.TypedTuple<Object>> typedTuples =
                redisTemplate.opsForZSet().rangeWithScores(KeyConstant.POST_KEY + id, 0, -1);
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok("取关成功", "");
        }
        List<Object> ids = typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getValue).toList();
        // 删除要取关用户的帖子
        String key = KeyConstant.POST_LIST_KEY + user.getId();
        for (Object o : ids) {
            redisTemplate.opsForZSet().remove(key, o);
        }
        return Result.ok("取关成功", "");
    }

    @Override
    public Result getFollowerList(Long id, Integer pageNum, Integer pageSize) {
        // 如果id为空 说明查询自己的粉丝列表
        if (id == null) {
            id = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername())).getId();
        } else {
            // 查询该用户粉丝列表是否为私密
            User u = userMapper.selectById(id);
            if (u.getFansPrivate()) {
                return Result.error(MessageConstant.USER_FANS_PRIVATE, List.of());
            }
        }
        return getUserList(id, pageNum, pageSize);
    }

    private Result getUserList (Long id, Integer pageNum, Integer pageSize) {
        List<Long> ids = Objects.requireNonNull(redisTemplate.opsForSet().members(KeyConstant.FANS_LIST_KEY + id))
                .stream().map(member -> Long.valueOf(member.toString())).toList();
        if (ids.isEmpty()) {
            return Result.ok(List.of());
        }
        if ((pageNum - 1) * pageSize > ids.size()) {
            return Result.ok(List.of());
        }
        // 获取当前用户
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername()));
        ids = ids.stream().skip((long) (pageNum - 1) * pageSize).limit(pageSize).toList();
        List<User> users = userMapper.selectByIds(ids);
        List<UserVO> userVOs = users.stream().map(user -> {
            UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
            userVO.setAuthority(authorityMapper.selectById(user.getAuthorityId()).getAuthority());
            Integer count = (Integer) redisTemplate.opsForValue().get(KeyConstant.FOLLOW_COUNT_KEY + user.getId());
            userVO.setCount(count == null ? 0 : count);
            if (u != null) {
                userVO.setFollowed(redisTemplate.opsForSet().isMember(KeyConstant.FOLLOW_LIST + u.getId(), user.getId()));
            } else {
                userVO.setFollowed(false);
            }
            return userVO;
        }).toList();
        return Result.ok(userVOs, (long) ids.size());
    }

    @Override
    public Result getFolloweeList(Long id, Integer pageNum, Integer pageSize) {
        if (id == null) {
            id = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername())).getId();
        } else {
            // 查询该用户关注列表是否为私密
            User u = userMapper.selectById(id);
            if (u.getFollowPrivate()) {
                return Result.error(MessageConstant.USER_FOLLOW_PRIVATE, List.of());
            }
        }
        // 查询该用户关注列表
        List<Long> ids = Objects.requireNonNull(redisTemplate.opsForSet().members(KeyConstant.FOLLOW_LIST + id))
                .stream().map(member -> Long.valueOf(member.toString())).toList();
        ids = ids.stream().skip((long) (pageNum - 1) * pageSize).limit(pageSize).toList();
        List<User> users = userMapper.selectByIds(ids);
        List<UserVO> userVOs = new ArrayList<>();
        for (User user : users) {
            UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
            userVO.setFollowed(true);
            Integer count = (Integer) redisTemplate.opsForValue().get(KeyConstant.FOLLOW_COUNT_KEY + user.getId());
            userVO.setCount(count == null ? 0 : count);
            userVO.setAuthority(authorityMapper.selectById(user.getAuthorityId()).getAuthority());
            userVOs.add(userVO);
        }
        return Result.ok(userVOs, (long) ids.size());
    }
}

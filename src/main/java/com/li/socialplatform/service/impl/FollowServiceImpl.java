package com.li.socialplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
            return Result.error(MessageConstant.USER_NOT_FOLLOWED, List.of());
        }
        // 粉丝数减一
        redisTemplate.opsForSet().remove(KeyConstant.FANS_LIST_KEY + id, user.getId());
        redisTemplate.opsForValue().increment(KeyConstant.FOLLOW_COUNT_KEY + id, -1);
        return Result.ok();
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
        List<Long> ids = Objects.requireNonNull(redisTemplate.opsForSet().members(KeyConstant.FANS_LIST_KEY + id))
                .stream().map(member -> Long.valueOf(member.toString())).toList();
        if (ids.isEmpty()) {
            return Result.ok(List.of());
        }
        if ((pageNum - 1) * pageSize > ids.size()) {
            return Result.ok(List.of());
        }
        ids = ids.stream().skip((long) (pageNum - 1) * pageSize).limit(pageSize).toList();
        List<User> users = userMapper.selectByIds(ids);
        List<UserVO> userVOs = users.stream().map(user -> new UserVO(user.getId(), user.getUsername(), user.getAvatar(),
                user.getBio(), user.getGender(), authorityMapper.selectById(user.getAuthorityId()).getAuthority(),
                user.getCreateTime(), user.getNickname())).toList();
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
        IPage<Follow> page = new Page<>(pageNum, pageSize);
        IPage<Follow> followIPage = followMapper.selectPage(page, new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, id));
        List<Follow> follows = followIPage.getRecords();
        if (follows.isEmpty()) {
            return Result.ok(List.of(), followIPage.getTotal());
        }
        List<Long> ids = follows.stream().map(Follow::getFolloweeId).toList();
        List<User> users = userMapper.selectByIds(ids);
        return Result.ok(users, followIPage.getTotal());
    }
}

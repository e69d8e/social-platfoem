package com.li.socialplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.properties.SystemConstants;
import com.li.socialplatform.mapper.AuthorityMapper;
import com.li.socialplatform.mapper.PostMapper;
import com.li.socialplatform.mapper.UserMapper;
import com.li.socialplatform.pojo.dto.SearchPostDTO;
import com.li.socialplatform.pojo.dto.SearchUserDTO;
import com.li.socialplatform.pojo.dto.UserDTO;
import com.li.socialplatform.pojo.entity.Post;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.pojo.entity.User;
import com.li.socialplatform.pojo.vo.PostVO;
import com.li.socialplatform.pojo.vo.UserVO;
import com.li.socialplatform.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author e69d8e
 * @since 2025/12/8 15:45
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserMapper userMapper;
    private final SystemConstants systemConstants;
    private final AuthorityMapper authorityMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMapper postMapper;

    // 密码加密
    private String encodePassword(String password) {
        // 密码加密
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String result = encoder.encode(password);
        return "{bcrypt}" + result;
    }

    @Override
    public Result register(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUsername() == null || userDTO.getPassword() == null) {
            return Result.error(MessageConstant.USER_IS_EMPTY);
        }
        // 使用正则校验
        if (!userDTO.getUsername().matches("^[a-zA-Z0-9_-]{4,16}$")) {
            return Result.error(MessageConstant.USERNAME_FORMAT_ERROR);
        }
        if (!userDTO.getPassword().matches("^[a-zA-Z0-9_-]{6,16}$")) {
            return Result.error(MessageConstant.PASSWORD_FORMAT_ERROR);
        }
        // 判断是否已经有该用户名
        User existingUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userDTO.getUsername()));
        if (existingUser != null) {
            return Result.error(MessageConstant.USERNAME_ALREADY_EXISTS);
        }
        User user = new User();
        // 使用UUID生成随机昵称
        String uuid = UUID.randomUUID().toString().replace("-", "");
        user.setNickname(systemConstants.userNicknamePrefix + uuid);
        user.setPassword(encodePassword(userDTO.getPassword()));
        user.setUsername(userDTO.getUsername());

        userMapper.insert(user);
        return Result.ok("注册成功", "");
    }

    // 获取当前登录用户的用户名
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @Override
    public Result getUserProfile(Long id) {
        User user;
        if (id == null) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername()));
        } else {
            user = userMapper.selectById(id);
            if (user == null) {
                return Result.error(MessageConstant.USER_NOT_FOUND);
            }
        }
        // 获取角色
        UserVO userVO = new UserVO();
        userVO.setAuthority(authorityMapper.selectById(user.getAuthorityId()).getAuthority());
        userVO.setCreateTime(user.getCreateTime());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setBio(user.getBio());
        userVO.setGender(user.getGender());
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        return Result.ok(userVO);
    }

    @Override
    public Result updateUserProfile(UserDTO userDTO) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername())
        );

        // 更新用户信息
        user.setNickname(userDTO.getNickname() == null ? user.getNickname() : userDTO.getNickname());
        user.setAvatar(userDTO.getAvatar() == null ? user.getAvatar() : userDTO.getAvatar());
        user.setBio(userDTO.getBio() == null ? user.getBio() : userDTO.getBio());
        user.setGender(userDTO.getGender() == null ? user.getGender() : userDTO.getGender());
        user.setFansPrivate(userDTO.getFansPrivate() == null ? user.getFansPrivate() : userDTO.getFansPrivate());
        user.setFollowPrivate(userDTO.getFollowPrivate() == null ? user.getFollowPrivate() : userDTO.getFollowPrivate());
        userMapper.updateById(user);
        return Result.ok("更新成功", "");
    }

    @Override
    public Result updatePassword(UserDTO userDTO) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername())
        );
        user.setPassword(encodePassword(userDTO.getPassword()));
        userMapper.updateById(user);
        return Result.ok("修改成功", "");
    }

    @Override
    public Result signIn() {
        LocalDateTime now = LocalDateTime.now();
        String key = KeyConstant.SIGN_IN_KEY + getCurrentUsername() + now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 获取今天是当月的第几天
        int day = now.getDayOfMonth();
        // 设置签到
        redisTemplate.opsForValue()
                .setBit(key, day - 1, true);
        // 计算当月截至今天连续签到次数
        List<Long> longs = redisTemplate.opsForValue().bitField(key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(day))
                        .valueAt(0) // 从第0位开始读取
        );
        // 0000000011
        Long tmp = null;
        if (longs != null) {
            tmp = longs.getFirst();
        }
        int count = 0;
        while (tmp != null && tmp != 0) {
            if ((tmp & 1) == 1) {
                count++;
            } else {
                break;
            }
            tmp = tmp >> 1;
        }
        return Result.ok(count);
    }

    @Override
    public Result listPost(SearchPostDTO searchPostDTO) {
        if (searchPostDTO.getPageNum() == null) {
            searchPostDTO.setPageNum(1);
        }
        if (searchPostDTO.getPageSize() == null) {
            searchPostDTO.setPageSize(10);
        }
        if (searchPostDTO.getSearch() == null) {
            searchPostDTO.setSearch("");
        }
        IPage<Post> page = new Page<>(searchPostDTO.getPageNum(), searchPostDTO.getPageSize());
        LambdaQueryWrapper<Post> search = new LambdaQueryWrapper<Post>()
                .like(Post::getContent, searchPostDTO.getSearch())
                .eq(Post::getEnabled, true);
        if (searchPostDTO.getCategoryId() != null) {
            search.eq(Post::getCategoryId, searchPostDTO.getCategoryId());
        }
        IPage<Post> postIPage = postMapper.selectPage(page, search);
        List<Post> records = postIPage.getRecords();
        if (records.isEmpty()) {
            return Result.ok(List.of(), postIPage.getTotal());
        }
        List<PostVO> postVOS = new ArrayList<>();
        for (Post record : records) {
            PostVO postVO = new PostVO();
            postVO.setPost(record);
            postVOS.add(postVO);
        }
        return Result.ok(postVOS, postIPage.getTotal());
    }

    @Override
    public Result listUser(SearchUserDTO searchUserDTO) {
        if (searchUserDTO.getPageNum() == null) {
            searchUserDTO.setPageNum(1);
        }
        if (searchUserDTO.getPageSize() == null) {
            searchUserDTO.setPageSize(10);
        }
        if (searchUserDTO.getNickname() == null) {
            searchUserDTO.setNickname("");
        }
        IPage<User> page = new Page<>(searchUserDTO.getPageNum(), searchUserDTO.getPageSize());
        LambdaQueryWrapper<User> search = new LambdaQueryWrapper<User>()
                .like(User::getNickname, searchUserDTO.getNickname());
        if (searchUserDTO.getGender() != null) {
            search.eq(User::getGender, searchUserDTO.getGender());
        }
        IPage<User> userIPage = userMapper.selectPage(page, search);
        List<User> records = userIPage.getRecords();
        List<UserVO> users = new ArrayList<>();
        for (User record : records) {
            UserVO userVO = new UserVO();
            userVO.setId(record.getId());
            userVO.setGender(record.getGender());
            userVO.setBio(record.getBio());
            userVO.setAvatar(record.getAvatar());
            userVO.setUsername(record.getUsername());
            userVO.setNickname(record.getNickname());
            userVO.setCreateTime(record.getCreateTime());
            userVO.setAuthority(authorityMapper.selectById(record.getAuthorityId()).getAuthority());
            users.add(userVO);
        }
        return Result.ok(users, userIPage.getTotal());
    }
}

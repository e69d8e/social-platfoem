package com.li.socialplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.properties.SystemConstants;
import com.li.socialplatform.mapper.AuthorityMapper;
import com.li.socialplatform.mapper.UserMapper;
import com.li.socialplatform.pojo.dto.UserDTO;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.pojo.entity.User;
import com.li.socialplatform.pojo.vo.UserVO;
import com.li.socialplatform.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author e69d8e
 * @since 2025/12/8 15:45
 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserMapper userMapper;
    private final SystemConstants systemConstants;
    private final AuthorityMapper authorityMapper;

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
        return Result.ok();
    }

    // 获取当前登录用户的用户名
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @Override
    public Result getUserProfile(Long id) {
        User user = null;
        if (id == null) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername()));
        } else {
            user = userMapper.selectById(id);
        }
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setBio(user.getBio());
        userVO.setGender(user.getGender());
        // 获取角色
        userVO.setAuthority(authorityMapper.selectById(user.getAuthorityId()).getAuthority());
        userVO.setCreateTime(user.getCreateTime());
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

        userMapper.updateById(user);
        return Result.ok();
    }

    @Override
    public Result updatePassword(UserDTO userDTO) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername())
        );
        user.setPassword(encodePassword(userDTO.getPassword()));
        userMapper.updateById(user);
        return Result.ok();
    }
}

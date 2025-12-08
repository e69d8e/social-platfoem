package com.li.socialplatform.controller;

import com.li.socialplatform.pojo.dto.UserDTO;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author e69d8e
 * @since 2025/12/8 14:30
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    // 注册
    @RequestMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        return userService.register(userDTO);
    }
    // 获取当前用户信息
    @RequestMapping("/profile")
    public Result getUserProfile() {
        return userService.getUserProfile(null);
    }
    // 获取用户信息
    @RequestMapping("/profile/{id}")
    public Result getUserProfile(@PathVariable Long id) {
        return userService.getUserProfile(id);
    }
    // 修改用户信息
    @PutMapping("/profile")
    public Result updateUserProfile(@RequestBody UserDTO userDTO) {
        return userService.updateUserProfile(userDTO);
    }
    // 修改密码
    @PutMapping("/password")
    public Result updatePassword(@RequestBody UserDTO userDTO) {
        return userService.updatePassword(userDTO);
    }
}

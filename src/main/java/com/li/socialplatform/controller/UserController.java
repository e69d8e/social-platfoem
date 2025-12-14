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

    // 签到 返回当月累计签到次数
    @PostMapping("/sign")
    public Result signIn() {
        return userService.signIn();
    }

    // 用户搜索帖子
    @GetMapping("/list/post")
    public Result listPost(@RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "8") Integer pageSize,
                           Integer id) {
        return userService.listPost(search, pageNum, pageSize, id);
    }

    // 用户搜索用户
    @GetMapping("/list/user")
    public Result listUser(@RequestParam(defaultValue = "") String nickname,
                           @RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize, Integer gender) {
        return userService.listUser(nickname, pageNum, pageSize, gender);
    }
}

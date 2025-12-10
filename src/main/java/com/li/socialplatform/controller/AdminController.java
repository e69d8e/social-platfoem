package com.li.socialplatform.controller;

import com.li.socialplatform.pojo.dto.SearchPostDTO;
import com.li.socialplatform.pojo.dto.SearchUserDTO;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author e69d8e
 * @since 2025/12/10 13:49
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminService adminService;

    @GetMapping("/user/list")
    public Result listAllUser(@RequestBody SearchUserDTO searchUserDTO) {
        return adminService.listAllUser(searchUserDTO);
    }

    @GetMapping("/post/list")
    public Result listAllPost(@RequestBody SearchPostDTO searchPostDTO) {
        return adminService.listAllPost(searchPostDTO);
    }

    // 封禁解封用户
    @PutMapping("/ban/{id}")
    public Result banUser(@PathVariable Long id) {
        return adminService.banUser(id);
    }

    // 将用户设为审核
    @PutMapping("/review/{id}")
    public Result setReviewer(@PathVariable Long id) {
        return adminService.setReviewer(id);
    }

    // 设为普通用户
    @PutMapping("/user/{id}")
    public Result setUser(@PathVariable Long id) {
        return adminService.setUser(id);
    }

    // 封禁帖子 解封
    @PutMapping("/post/{id}")
    public Result banPost(@PathVariable Long id) {
        return adminService.banPost(id);
    }

    // 删除评论
    @DeleteMapping("/comment/{id}/{postId}")
    public Result deleteComment(@PathVariable Long id, @PathVariable Long postId) {
        return adminService.deleteComment(id, postId);
    }
}

package com.li.socialplatform.controller;

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

    // 封禁解封用户
    @PutMapping("/ban/{id}")
    public Result banUser(@PathVariable Long id) {
        return adminService.banUser(id);
    }

    // 获取封禁用户
    @GetMapping("/ban")
    public Result getBanUser(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "8") Integer pageSize
    ) {
        return adminService.getBanUser(pageNum, pageSize);
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


}

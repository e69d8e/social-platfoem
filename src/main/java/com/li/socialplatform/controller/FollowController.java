package com.li.socialplatform.controller;

import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.IFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author e69d8e
 * @since 2025/12/8 23:00
 */
@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final IFollowService followService;
    // 关注用户
    @PostMapping("/{id}")
    public Result follow(@PathVariable Long id) {
        return followService.follow(id);
    }
    // 取消关注
    @DeleteMapping("/{id}")
    public Result cancelFollow(@PathVariable Long id) {
        return followService.cancelFollow(id);
    }
}

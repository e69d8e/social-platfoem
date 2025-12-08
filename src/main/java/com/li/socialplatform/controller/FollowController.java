package com.li.socialplatform.controller;

import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.IFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

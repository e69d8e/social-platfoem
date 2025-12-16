package com.li.socialplatform.controller;

import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.IReviewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author e69d8e
 * @since 2025/12/10 13:47
 */
@RestController
@RequestMapping("/reviewer")
@RequiredArgsConstructor
public class ReviewerController {

    private final IReviewerService reviewerService;

    // 封禁帖子 解封帖子
    @PutMapping("/post/{id}")
    public Result banPost(@PathVariable Long id) {
        return reviewerService.banPost(id);
    }

    // 查询当前用户封禁的帖子
    @GetMapping("/post/ban")
    public Result listBanPost(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "8") Integer pageSize
    ) {
        return reviewerService.listBanPost(pageNum, pageSize);
    }
}

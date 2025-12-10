package com.li.socialplatform.controller;

import com.li.socialplatform.pojo.dto.SearchPostDTO;
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

    @GetMapping("/post/list")
    public Result listAllPost(@RequestBody SearchPostDTO searchPostDTO) {
        return reviewerService.listAllPost(searchPostDTO);
    }

    // 封禁帖子 解封帖子
    @PutMapping("/post/{id}")
    public Result banPost(@PathVariable Long id) {
        return reviewerService.banPost(id);
    }

    // 删除评论
    @DeleteMapping("/comment/{id}/{postId}")
    public Result deleteComment(@PathVariable Long id, @PathVariable Long postId) {
        return reviewerService.deleteComment(id, postId);
    }
}

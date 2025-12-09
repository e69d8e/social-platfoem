package com.li.socialplatform.controller;

import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.ILikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * @author e69d8e
 * @since 2025/12/9 21:40
 */
@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {

    private final ILikeService likeService;

    @PutMapping("/{postId}")
    public Result like(@PathVariable Long postId) {
        return likeService.like(postId);
    }
}

package com.li.socialplatform.controller;

import com.li.socialplatform.pojo.dto.PostDTO;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.IPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * @author e69d8e
 * @since 2025/12/9 14:21
 */
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final IPostService postService;
    // 发布帖子
    @PostMapping
    public Result publishPost(@RequestBody PostDTO postDTO) {
        return postService.publishPost(postDTO);
    }
    // 获取帖子详情
    @GetMapping("/{id}")
    public Result getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }
    // 首页帖子列表
    @GetMapping("/list")
    public Result listPosts(@RequestParam Long lastId, @RequestParam(defaultValue = "0") Integer offset) {
        return postService.listPosts(lastId,  offset);
    }
    // 关注帖子列表
    @GetMapping("/follow/list")
    public Result listFollowPosts(@RequestParam Long lastId, @RequestParam(defaultValue = "0") Integer offset) {
        return postService.listFollowPosts(lastId, offset);
    }
}

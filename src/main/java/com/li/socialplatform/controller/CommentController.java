package com.li.socialplatform.controller;

import com.li.socialplatform.pojo.dto.CommentDTO;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author e69d8e
 * @since 2025/12/9 18:17
 */
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    private final ICommentService commentService;

    @PostMapping
    public Result addComment(@RequestBody CommentDTO commentDTO) {
        return commentService.addComment(commentDTO);
    }

    // 获取一级评论
    @GetMapping("/post/{id}")
    public Result getComments(@PathVariable Long id,
                              @RequestParam Long lastId,
                              @RequestParam(defaultValue = "0") Integer offset) {
        return commentService.getComments(id, lastId, offset);
    }

    // 根据一级评论id获取评论
    @GetMapping("/post/{id}/{commentId}")
    public Result getComments(@PathVariable(value = "id") Long postId, @PathVariable Long commentId) {
        return commentService.getTwoComments(postId, commentId);
    }
}

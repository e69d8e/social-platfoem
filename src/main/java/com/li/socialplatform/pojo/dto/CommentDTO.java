package com.li.socialplatform.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author e69d8e
 * @since 2025/12/9 18:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO implements Serializable {
    private Long postId;
    private String content;
    private Long parentId;
    private Long replyTo;
}
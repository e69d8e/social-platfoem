package com.li.socialplatform.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author e69d8e
 * @since 2026/03/21 21:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Long postId;
    private String content;
    private String title;
}

package com.li.socialplatform.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author e69d8e
 * @since 2025/12/9 18:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("comment")
public class CommentVO implements Serializable {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private String createTime;
}

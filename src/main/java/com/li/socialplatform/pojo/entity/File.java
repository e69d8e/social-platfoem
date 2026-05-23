package com.li.socialplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author e69d8e
 * @since 2026/05/22 19:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("file")
public class File {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "post_id")
    private Long postId;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "url")
    private String url;

    @TableField(value = "hash")
    private String hash;
}

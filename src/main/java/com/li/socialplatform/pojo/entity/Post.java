package com.li.socialplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post implements Serializable {

    @TableId(value = "id", type = IdType.AUTO) // 帖子ID通常使用数据库自增
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("content")
    private String content;

    @TableField("category_id")
    private String categoryId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
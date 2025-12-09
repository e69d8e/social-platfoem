package com.li.socialplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @TableField("user_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    @TableField("content")
    private String content;

    @TableField("category_id")
    private Integer categoryId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "enable")
    private Boolean enable;
}
package com.li.socialplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("follow")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Follow implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @TableField("follower_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long followerId; // 关注者id

    @TableField("followee_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long followeeId; // 被关注的用户id

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
package com.li.socialplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author e69d8e
 * @since 2025/12/9 19:22
 */
@TableName("follow")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Follow implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("follower_id")
    private Long followerId; // 关注者id

    @TableField("followee_id")
    private Long followeeId; // 被关注的用户id

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
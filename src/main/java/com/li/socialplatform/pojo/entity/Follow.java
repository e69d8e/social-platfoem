package com.li.socialplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
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
    private Long id;

    @TableField("follower_id")
    private Long followerId;

    @TableField("followee_id")
    private Long followeeId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
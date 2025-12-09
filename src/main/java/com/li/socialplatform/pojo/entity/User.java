package com.li.socialplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author e69d8e
 * @since 2025/12/8 14:31
 */
@TableName("user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "username")
    private String username;

    @TableField(value = "nickname")
    private String nickname;

    @TableField(value = "password")
    private String password; // 存储加密后的密码

    @TableField(value = "avatar")
    private String avatar;

    @TableField(value = "bio")
    private String bio;

    @TableField(value = "gender")
    private Integer gender;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "authority_id")
    private Integer authorityId;

    @TableField(value = "enabled")
    private Boolean enabled;

    @TableField(value = "fans_private")
    private Boolean fansPrivate; // 是否允许他人查看粉丝列表 (0: 允许, 1: 不允许)

    @TableField(value = "follow_private")
    private Boolean followPrivate; // 是否允许他人查看关注列表 (0: 允许, 1: 不允许)
}

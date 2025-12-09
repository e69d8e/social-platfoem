package com.li.socialplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author e69d8e
 * @since 2025/12/9 21:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("like_record")
public class LikeRecord implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    @TableField(value = "post_id")
    private Long postId;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "create_time")
    private LocalDateTime createTime;
}

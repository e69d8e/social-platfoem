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

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("ban_record")
public class BanRecord implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 执行封禁操作的管理员/审查员ID */
    @TableField(value = "user_id")
    private Long userId;
    /** 被封禁的目标ID（用户或帖子） */
    @TableField(value = "target_id")
    private Long targetId;
    /** 0=用户封禁, 1=帖子封禁 */
    @TableField(value = "type")
    private Integer type;
    @TableField(value = "create_time")
    private LocalDateTime createTime;
}

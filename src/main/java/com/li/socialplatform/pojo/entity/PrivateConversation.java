package com.li.socialplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author e69d8e
 * @since 2025/12/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("private_conversation")
public class PrivateConversation implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @TableField(value = "user_a_id")
    private Long userAId;
    @TableField(value = "user_b_id")
    private Long userBId;
    @TableField(value = "last_message")
    private String lastMessage;
    @TableField(value = "last_message_time")
    private LocalDateTime lastMessageTime;
    @TableField(value = "unread_a")
    private Integer unreadA;
    @TableField(value = "unread_b")
    private Integer unreadB;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

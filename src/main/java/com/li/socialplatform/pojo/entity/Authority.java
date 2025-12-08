package com.li.socialplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author e69d8e
 * @since 2025/12/8 15:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("authority")
public class Authority implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "authority")
    private String authority;
}

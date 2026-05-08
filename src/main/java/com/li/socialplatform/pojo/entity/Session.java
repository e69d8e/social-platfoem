package com.li.socialplatform.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "会话")
@TableName("session")
public class Session {
    @Id
    @Schema(description = "记忆id")
    private String id;

    @TableField(value = "name")
    @Schema(description = "名称")
    private String name;

    @TableField(value = "user_id")
    @Schema(description = "用户id")
    private Long userId;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @TableField(value = "time", fill = FieldFill.INSERT)
    @Schema(description = "时间")
    private LocalDateTime time;
}

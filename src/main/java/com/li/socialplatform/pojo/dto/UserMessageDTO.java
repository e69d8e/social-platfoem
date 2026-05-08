package com.li.socialplatform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户消息")
@ToString
public class UserMessageDTO {
    @Schema(description = "用户id")
    private Long userId;
    @Schema(description = "记忆id")
    private String memoryId;
    @Schema(description = "内容")
    private String content;
}

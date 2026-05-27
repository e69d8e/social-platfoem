package com.li.socialplatform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author e69d8e
 * @since 2025/12/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "发送私信请求")
public class SendMessageDTO implements Serializable {
    @Schema(description = "接收方用户ID", example = "2")
    private Long receiverId;
    @Schema(description = "消息内容", example = "你好，很高兴认识你！")
    private String content;
}

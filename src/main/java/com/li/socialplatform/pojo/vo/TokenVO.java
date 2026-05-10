package com.li.socialplatform.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author e69d8e
 * @since 2026/05/10 14:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenVO {
    private String accessToken;
    private String refreshToken;
}

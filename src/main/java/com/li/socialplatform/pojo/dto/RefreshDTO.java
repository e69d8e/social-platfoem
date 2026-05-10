package com.li.socialplatform.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author e69d8e
 * @since 2026/05/10 15:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshDTO {
    private String refreshToken;
}

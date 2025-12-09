package com.li.socialplatform.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author e69d8e
 * @since 2025/12/8 17:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements Serializable {
    private String username;
    private String password;

    private String nickname;
    private String avatar;
    private String bio;
    private Integer gender;
    // 仅管理员
    private String authority;
    private Boolean enabled;

    private Boolean fansPrivate;
    private Boolean followPrivate;
}

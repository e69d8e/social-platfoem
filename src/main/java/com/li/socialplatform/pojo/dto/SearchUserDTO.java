package com.li.socialplatform.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author e69d8e
 * @since 2025/12/10 15:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchUserDTO implements Serializable {
    private String username;
    private String nickname;
    private Integer authorityId;
    private Boolean enabled;
    private String gender;
    private Integer pageNum;
    private Integer pageSize;
}

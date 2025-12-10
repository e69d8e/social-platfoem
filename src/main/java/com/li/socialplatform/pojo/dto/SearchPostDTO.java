package com.li.socialplatform.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author e69d8e
 * @since 2025/12/10 15:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchPostDTO implements Serializable {
    private String search;
    private Integer categoryId;
    private Boolean enabled;
    private Integer pageNum;
    private Integer pageSize;
}

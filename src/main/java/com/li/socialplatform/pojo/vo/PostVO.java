package com.li.socialplatform.pojo.vo;

import com.li.socialplatform.pojo.entity.Post;
import com.li.socialplatform.pojo.entity.PostImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author e69d8e
 * @since 2025/12/9 15:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostVO implements Serializable {
    private Post post;

    private String categoryName;

    private UserVO user;

    private List<PostImage> postImages;
}

package com.li.socialplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.socialplatform.pojo.dto.PostDTO;
import com.li.socialplatform.pojo.entity.Post;
import com.li.socialplatform.pojo.entity.Result;

public interface IPostService extends IService<Post> {
    Result publishPost(PostDTO postDTO);

    Result getPost(Long id);

    Result listPosts(Long lastId, Integer offset);

    Result listFollowPosts(Long lastId, Integer offset);
}

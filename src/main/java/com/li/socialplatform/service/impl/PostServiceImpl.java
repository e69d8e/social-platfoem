package com.li.socialplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.socialplatform.mapper.PostMapper;
import com.li.socialplatform.pojo.entity.Post;
import com.li.socialplatform.service.IPostService;
import org.springframework.stereotype.Service;

/**
 * @author e69d8e
 * @since 2025/12/9 14:22
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {
}

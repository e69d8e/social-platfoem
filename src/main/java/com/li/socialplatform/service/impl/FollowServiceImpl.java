package com.li.socialplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.socialplatform.mapper.FollowMapper;
import com.li.socialplatform.pojo.entity.Follow;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.IFollowService;
import org.springframework.stereotype.Service;

/**
 * @author e69d8e
 * @since 2025/12/8 23:02
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {
    @Override
    public Result follow(Long id) {
        return null;
    }
}

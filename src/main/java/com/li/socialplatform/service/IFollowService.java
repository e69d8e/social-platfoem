package com.li.socialplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.socialplatform.pojo.entity.Follow;
import com.li.socialplatform.pojo.entity.Result;

public interface IFollowService extends IService<Follow> {
    Result follow(Long id);

    Result cancelFollow(Long id);

    Result getFollowerList(Long id, Integer pageNum, Integer pageSize);

    Result getFolloweeList(Long id, Integer pageNum, Integer pageSize);
}

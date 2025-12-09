package com.li.socialplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.socialplatform.pojo.entity.LikeRecord;
import com.li.socialplatform.pojo.entity.Result;

public interface ILikeService extends IService<LikeRecord> {
    Result like(Long postId);
}

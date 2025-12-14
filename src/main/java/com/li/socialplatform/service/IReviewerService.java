package com.li.socialplatform.service;

import com.li.socialplatform.pojo.entity.Result;

public interface IReviewerService {
    Result banPost(Long id);

    Result listBanPost(String pageNum, Integer pageSize);
}

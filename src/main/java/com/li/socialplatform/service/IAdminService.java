package com.li.socialplatform.service;

import com.li.socialplatform.pojo.entity.Result;

public interface IAdminService {
    Result banUser(Long id);

    Result getBanUser(Integer pageNum, Integer pageSize);

    Result setReviewer(Long id);

    Result setUser(Long id);
}

package com.li.socialplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.socialplatform.pojo.entity.Category;
import com.li.socialplatform.pojo.entity.Result;

public interface ICategoryService extends IService<Category> {
    Result getCategory();
}

package com.li.socialplatform.controller;

import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author e69d8e
 * @since 2025/12/12 22:24
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;
    // 获取分类
    @GetMapping
    public Result getCategory() {
        return categoryService.getCategory();
    }

}

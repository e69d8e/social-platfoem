package com.li.socialplatform.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author e69d8e
 * @since 2025/12/8 16:18
 */
@RestController
@Slf4j
public class TestController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("test1")
    public String test1() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("用户信息：{}", auth.getName());
//        throw new BizException("hhh");
        return "test1";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("test3")
    public String test3() {
//        throw new MethodArgumentNotValidException("hhh");
        return "test3";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("test2")
    public String test2() {
//        throw new RuntimeException("hhh");
        return "test2";
    }
}

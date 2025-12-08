package com.li.socialplatform.handler;

import com.alibaba.fastjson2.JSON;
import com.li.socialplatform.pojo.entity.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal(); // 获取用户信息
//        Object credentials = authentication.getCredentials(); // 获取用户凭证
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities(); // 获取用户权限
        // 封装响应数据
        Object json = JSON.toJSON(Result.ok());
        // 设置响应头
        response.setContentType("application/json;charset=UTF-8");
        // 设置响应状态码
        response.setStatus(200);
        // 响应数据
        response.getWriter().write(json.toString());
    }
}

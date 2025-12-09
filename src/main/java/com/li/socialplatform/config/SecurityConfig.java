package com.li.socialplatform.config;

import com.li.socialplatform.handler.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity // 开启方法权限控制
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //authorizeRequests()：开启授权保护
        //anyRequest()：对所有请求开启授权保护
        //authenticated()：已认证请求会自动被授权
        http.authorizeHttpRequests(
                authorize ->
                        authorize
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/doc.html",
                                        "/webjars/**",
                                        "/user/register",
                                        "/upload/**"
                                ).permitAll() // 放行swagger
                                .anyRequest() // 所有请求
                                .authenticated()) // 已认证请求会自动被授权
                .formLogin(
                        login -> login
                                        .loginProcessingUrl("/user/login").permitAll() // 前端登录接口
                                        .successHandler(new MyAuthenticationSuccessHandler()) // 登录成功处理
                                        .failureHandler(new MyAuthenticationFailHandler()) // 登录失败处理
                )// 自定义登录页面 .permitAll() 表示登录页面可以任意访问
        ;
        // 注销
        http.logout(logout -> logout
                .logoutSuccessHandler(new MyLogoutHandler()) // 注销成功处理
                .logoutUrl("/user/logout") // 注销接口 post 请求
        );
        // 错误信息
        http.exceptionHandling(
                exception -> {
                    exception.authenticationEntryPoint(new MyAuthenticationEntryPoint());
                    exception.accessDeniedHandler(new MyAccessDeniedHandler());// 拒绝访问处理 用户没有访问权限的时候
                }

        );
        // 跨域
        http.cors(withDefaults());
        // 关闭csrf攻击防御
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

}

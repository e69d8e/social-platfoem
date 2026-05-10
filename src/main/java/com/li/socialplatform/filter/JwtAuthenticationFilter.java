package com.li.socialplatform.filter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.li.socialplatform.common.utils.JwtUtils;
import com.li.socialplatform.pojo.entity.User;
import com.li.socialplatform.server.mapper.AuthorityMapper;
import com.li.socialplatform.server.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


/**
 * @author e69d8e
 * @since 2026/05/10 15:14
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final AuthorityMapper authorityMapper;
    // ... existing code ...
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain chain) throws IOException, ServletException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Claims claims = jwtUtils.parseToken(token);
                String username = claims.getSubject();
                log.debug("Token 解析成功, 用户名: {}", username);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
                    if (user != null) {
                        Integer authorityId = user.getAuthorityId();
                        String role = authorityMapper.selectById(authorityId).getAuthority();
                        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix));
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        log.warn("用户不存在: {}", username);
                    }
                }
            } catch (JwtException e) {
                logger.warn("JWT token 无效或已过期: " + e.getMessage());
            } catch (Exception e) {
                logger.error("处理 JWT token 时发生错误: " + e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}

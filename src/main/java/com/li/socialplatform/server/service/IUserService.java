package com.li.socialplatform.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.socialplatform.pojo.dto.LoginDTO;
import com.li.socialplatform.pojo.dto.RefreshDTO;
import com.li.socialplatform.pojo.dto.UserDTO;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.pojo.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface IUserService extends IService<User> {
    Result login(LoginDTO loginDTO);

    Result refresh(RefreshDTO refreshDTO, HttpServletRequest request);

    Result register(UserDTO userDTO);

    Result getUserProfile(Long id);

    Result updateUserProfile(UserDTO userDTO);

    Result updatePassword(UserDTO userDTO);

    Result signIn();

    Result signInCount();

    Result listPost(String keyword, Integer pageNum, Integer pageSize, Integer categoryId);

    Result listUser(String keyword, Integer pageNum, Integer pageSize);

    Result logout(HttpServletRequest request);
}

package com.li.socialplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.socialplatform.pojo.dto.UserDTO;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.pojo.entity.User;

public interface IUserService extends IService<User> {
    Result register(UserDTO userDTO);

    Result getUserProfile(Long id);

    Result updateUserProfile(UserDTO userDTO);

    Result updatePassword(UserDTO userDTO);

    Result signIn();
}

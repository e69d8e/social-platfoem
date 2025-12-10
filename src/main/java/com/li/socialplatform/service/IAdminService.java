package com.li.socialplatform.service;

import com.li.socialplatform.pojo.dto.SearchPostDTO;
import com.li.socialplatform.pojo.dto.SearchUserDTO;
import com.li.socialplatform.pojo.entity.Result;

public interface IAdminService {
    Result banUser(Long id);

    Result setReviewer(Long id);

    Result setUser(Long id);

    Result banPost(Long id);

    Result deleteComment(Long id, Long postId);

    Result listAllPost(SearchPostDTO searchPostDTO);

    Result listAllUser(SearchUserDTO searchUserDTO);
}

package com.li.socialplatform.service;

import com.li.socialplatform.pojo.dto.SearchPostDTO;
import com.li.socialplatform.pojo.entity.Result;

public interface IReviewerService {
    Result banPost(Long id);

    Result deleteComment(Long id, Long postId);

    Result listAllPost(SearchPostDTO searchPostDTO);
}

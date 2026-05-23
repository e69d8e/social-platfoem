package com.li.socialplatform.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.utils.BanCacheUtil;
import com.li.socialplatform.common.utils.DataCacheUtil;
import com.li.socialplatform.common.utils.UserIdUtil;
import com.li.socialplatform.server.mapper.CommentMapper;
import com.li.socialplatform.server.mapper.PostMapper;
import com.li.socialplatform.pojo.entity.Post;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.pojo.vo.PostVO;
import com.li.socialplatform.server.service.IReviewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author e69d8e
 * @since 2025/12/10 14:01
 */
@Service
@RequiredArgsConstructor
public class ReviewerServiceImpl implements IReviewerService {

    private final PostMapper postMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserIdUtil userIdUtil;
    private final CommentMapper commentMapper;
    private final DataCacheUtil dataCacheUtil;
    private final BanCacheUtil banCacheUtil;

    @Override
    public Result banPost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error(MessageConstant.POST_NOT_EXIST);
        }
        Long reviewerId = userIdUtil.getUserId();
        post.setEnabled(!post.getEnabled());
        if (!post.getEnabled()) {
            banCacheUtil.addBanPost(reviewerId, id);
        } else {
            banCacheUtil.removeBanPost(reviewerId, id);
        }
        return postMapper.updateById(post) > 0 ? Result.ok(MessageConstant.BAN_SUCCESS, "") : Result.error(MessageConstant.BAN_FAIL);
    }

    @Override
    public Result listBanPost(Integer pageNum, Integer pageSize) {
        long start = ((long) (pageNum - 1) * pageSize);
        long end = start + pageSize - 1;
        Long userId = userIdUtil.getUserId();
        Long total = banCacheUtil.getBanPostTotal(userId);
        if (total == null || total == 0) {
            return Result.ok(List.of(), 0L);
        }
        if (start > total) {
            return Result.ok(List.of(), 0L);
        }
        if (end > total) {
            end = total - 1;
        }
        Set<Object> members = banCacheUtil.getBanPostIds(userId, start, end);
        if (members == null || members.isEmpty()) {
            return Result.ok(List.of(), 0L);
        }
        List<Long> ids = members.stream().map(member -> Long.valueOf(member.toString())).toList();
        List<PostVO> postVOS = new ArrayList<>();
        for (Long id : ids) {
            Post post = postMapper.selectById(id);
            PostVO postVO = BeanUtil.copyProperties(post, PostVO.class);
            postVO.setLikeCount(dataCacheUtil.getLikeCount(id));
            if (userId != null) {
                postVO.setLiked(dataCacheUtil.isLiked(id, userId));
            } else {
                postVO.setLiked(false);
            }
            postVO.setEnabled(false);
            postVOS.add(postVO);
        }
        return Result.ok(postVOS, total);
    }

    @Override
    public Result deleteComment(Long id, Long postId) {
        redisTemplate.opsForZSet().remove(KeyConstant.COMMENT_KEY + postId, id);
        commentMapper.deleteById(id);
        return Result.ok(MessageConstant.DELETE_SUCCESS, "");
    }

}

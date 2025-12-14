package com.li.socialplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.properties.SystemConstants;
import com.li.socialplatform.common.utils.UserIdUtil;
import com.li.socialplatform.mapper.PostImageMapper;
import com.li.socialplatform.mapper.PostMapper;
import com.li.socialplatform.pojo.entity.*;
import com.li.socialplatform.pojo.vo.PostImageVO;
import com.li.socialplatform.pojo.vo.PostVO;
import com.li.socialplatform.service.IReviewerService;
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
    private final PostImageMapper postImageMapper;
    private final SystemConstants systemConstants;
    private final UserIdUtil userIdUtil;

    @Override
    public Result banPost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error(MessageConstant.POST_NOT_EXIST);
        }
        post.setEnabled(!post.getEnabled());
        // 将封禁的帖子加入缓存中
        if (!post.getEnabled()) {
            redisTemplate.opsForSet().add(KeyConstant.BAN_POST_KEY + userIdUtil.getUserId(), id);
        } else {
            redisTemplate.opsForSet().remove(KeyConstant.BAN_POST_KEY + userIdUtil.getUserId(), id);
        }
        // 更新数据库
        return postMapper.updateById(post) > 0 ? Result.ok(MessageConstant.BAN_SUCCESS, "") : Result.error(MessageConstant.BAN_FAIL);
    }

    @Override
    public Result listBanPost(String pageNum, Integer pageSize) {
        Long userId = userIdUtil.getUserId();
        Set<Object> members = redisTemplate.opsForSet().members(KeyConstant.BAN_POST_KEY + userId);
        if (members == null || members.isEmpty()) {
            return Result.ok(List.of(), 0L);
        }
        List<Long> ids = members.stream().map(member -> Long.valueOf(member.toString())).toList();
        List<PostVO> postVOS = new ArrayList<>();
        for (Long id : ids) {
            Post post = postMapper.selectById(id);
            PostVO postVO = BeanUtil.copyProperties(post, PostVO.class);
            List<PostImage> postImages = postImageMapper.selectList(new LambdaQueryWrapper<PostImage>().eq(PostImage::getPostId, id));
            postVO.setImgUrl(getImgUrl(postImages));
            postVO.setPostImages(postImagesToPostImagesVOs(postImages));
            postVO.setCount((Integer) redisTemplate.opsForValue().get(KeyConstant.LIKE_COUNT + id));
            if (userId != null) {
                postVO.setLiked(redisTemplate.opsForSet().isMember(KeyConstant.LIKE_KEY + id, userId));
            } else {
                postVO.setLiked(false);
            }
            postVO.setEnabled(false);
            postVOS.add(postVO);
        }
        return Result.ok(postVOS, Long.valueOf(ids.size()));
    }

    private List<PostImageVO> postImagesToPostImagesVOs(List<PostImage> postImages) {
        List<PostImageVO> postImageVOS = new ArrayList<>();
        for (PostImage postImage : postImages) {
            postImageVOS.add(BeanUtil.copyProperties(postImage, PostImageVO.class));
        }
        return postImageVOS;
    }

    private String getImgUrl(List<PostImage> postImages) {
        if (postImages == null || postImages.isEmpty()) {
            return systemConstants.defaultPostImg;
        }
        return postImages.getFirst().getUrl();
    }

}

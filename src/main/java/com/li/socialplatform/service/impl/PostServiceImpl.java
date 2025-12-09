package com.li.socialplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.properties.SystemConstants;
import com.li.socialplatform.mapper.*;
import com.li.socialplatform.pojo.dto.PostDTO;
import com.li.socialplatform.pojo.entity.*;
import com.li.socialplatform.pojo.vo.PostVO;
import com.li.socialplatform.pojo.vo.UserVO;
import com.li.socialplatform.service.IPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author e69d8e
 * @since 2025/12/9 14:22
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SystemConstants systemConstants;
    private final AuthorityMapper authorityMapper;

    // 获取当前登录用户的用户名
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
    private final UserMapper userMapper;

    @Override
    public Result publishPost(PostDTO postDTO) {
        // 获取当前登录用户id
        Long id = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, getCurrentUsername())).getId();
        if (id == null) {
            return Result.error(MessageConstant.USER_NOT_FOUND);
        }
        if (postDTO.getContent() == null || postDTO.getContent().isEmpty()) {
            return Result.error(MessageConstant.CONTENT_IS_NULL);
        }
        Post post = new Post();
        post.setUserId(id);
        post.setContent(postDTO.getContent());
        post.setCategoryId(postDTO.getCategoryId() == null ? 1 : postDTO.getCategoryId());
        postMapper.insert(post);
        log.info("用户 {} 发表了帖子 {}", id, post.getId());
        // 添加帖子图片
        if (postDTO.getImages() != null && !postDTO.getImages().isEmpty()) {
            for (String image : postDTO.getImages()) {
                postImageMapper.insert(new PostImage(null, post.getId(), image));
            }
        }
        long time = System.currentTimeMillis();
        // 将帖子添加到缓存中
        redisTemplate.opsForZSet().add(KeyConstant.POST_LIST_KEY, post.getId(), time);
        // 查询所有粉丝
        Set<Object> fans = redisTemplate.opsForSet().members(KeyConstant.FANS_LIST_KEY + id);
        if (fans == null || fans.isEmpty()) {
            return Result.ok();
        }
        // 解析
        List<Long> fanIds = fans.stream().map(fan -> Long.valueOf(fan.toString())).toList();
        // 将帖子添加到粉丝缓存
        fanIds.forEach(fanId -> redisTemplate.opsForZSet().add(KeyConstant.POST_LIST_KEY + fanId, post.getId(), time));
        return Result.ok();
    }

    @Override
    public Result getPost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null || !post.getEnabled()) {
            return Result.error(MessageConstant.POST_NOT_EXIST);
        }
        User user = userMapper.selectById(post.getUserId());
        List<PostImage> postImages = postImageMapper.selectList(new LambdaQueryWrapper<PostImage>().eq(PostImage::getPostId, id));
        PostVO postVO = new PostVO();
        postVO.setPost(post);
        postVO.setPostImages(postImages);
        postVO.setUser(getUserVO(user));
        postVO.setCategoryName(categoryMapper.selectById(post.getCategoryId()).getName());
        // 查询是否点过赞
        Long userId = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername())).getId();
        postVO.setLiked(redisTemplate.opsForSet().isMember(KeyConstant.LIKE_KEY + id, userId));
        return Result.ok(postVO);
    }

    @Override
    public Result listPosts(Long lastId, Integer offset) {
        // 获取当前用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername()));
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(KeyConstant.POST_LIST_KEY,
                0, lastId, offset, Long.parseLong(systemConstants.defaultPageSize));
        return Result.ok(getScrollResult(typedTuples, user.getId()));
    }

    @Override
    public Result listFollowPosts(Long lastId, Integer offset) {
        // 获取当前用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername()));
        //
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(KeyConstant.POST_LIST_KEY + user.getId(),
                        0, lastId, offset, Long.parseLong(systemConstants.defaultPageSize));
        return Result.ok(getScrollResult(typedTuples, user.getId()));
    }

    private ScrollResult<PostVO> getScrollResult(Set<ZSetOperations.TypedTuple<Object>> typedTuples, Long userId) {
        if (typedTuples == null || typedTuples.isEmpty()) {
            ScrollResult<PostVO> objectScrollResult = new ScrollResult<>();
            objectScrollResult.setList(new ArrayList<>());
            objectScrollResult.setMinTime(0L);
            objectScrollResult.setOffset(1);
            return objectScrollResult;
        }
        // 获取 id
        Set<Object> collect = typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getValue).collect(Collectors.toSet());
        // 解析id
        List<Long> ids = collect.stream().map(id -> Long.parseLong(id.toString())).toList();
        // 获取 score
        List<Double> scores = typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getScore).toList();
        List<PostVO> postVOS = new ArrayList<>();
        for (Long id : ids) {
            PostVO postVO = new PostVO();
            postVO.setPost(postMapper.selectById(id));
            postVO.setCategoryName(categoryMapper.selectById(postVO.getPost().getCategoryId()).getName());
            postVO.setUser(getUserVO(userMapper.selectById(postVO.getPost().getUserId())));
            postVO.setPostImages(postImageMapper.selectList(
                    new LambdaQueryWrapper<PostImage>().eq(PostImage::getPostId, id)));
            postVO.setLiked(redisTemplate.opsForSet().isMember(KeyConstant.LIKE_KEY + id, userId));
            postVOS.add(postVO);
        }
        int nweOffset = 1;
        double score = scores.getFirst();
        for (int i = 1; i < scores.size(); i++) {
            if (score == scores.get(i)) {
                nweOffset++;
            } else {
                nweOffset = 1;
            }
            score = scores.get(i);
        }
        ScrollResult<PostVO> postVOScrollResult = new ScrollResult<>();
        postVOScrollResult.setList(postVOS);
        postVOScrollResult.setOffset(nweOffset);
        postVOScrollResult.setMinTime(scores.getLast().longValue());
        return postVOScrollResult;
    }
    private UserVO getUserVO(User user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setBio(user.getBio());
        userVO.setGender(user.getGender());
        userVO.setCreateTime(user.getCreateTime());
        userVO.setAuthority(authorityMapper.selectById(user.getId()).getAuthority());
        return userVO;
    }
}

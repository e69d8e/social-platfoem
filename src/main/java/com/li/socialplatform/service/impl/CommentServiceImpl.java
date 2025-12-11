package com.li.socialplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.properties.SystemConstants;
import com.li.socialplatform.mapper.CommentMapper;
import com.li.socialplatform.mapper.UserMapper;
import com.li.socialplatform.pojo.dto.CommentDTO;
import com.li.socialplatform.pojo.entity.Comment;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.pojo.entity.ScrollResult;
import com.li.socialplatform.pojo.entity.User;
import com.li.socialplatform.service.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author e69d8e
 * @since 2025/12/9 18:28
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SystemConstants systemConstants;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    // 获取当前登录用户的用户名
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
    @Override
    public Result addComment(CommentDTO commentDTO) {
        Comment comment = BeanUtil.copyProperties(commentDTO, Comment.class);
        comment.setUserId(userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername())).getId());
        comment.setCreateTime(LocalDateTime.now().toString());
        // 保存到数据库
        commentMapper.insert(comment);
        // 保存到缓存
        // 如果是回复其他人的不缓存
        if (comment.getReplyTo() == null) {
            redisTemplate.opsForZSet().add(KeyConstant.COMMENT_KEY + commentDTO.getPostId() + comment.getId(), comment, System.currentTimeMillis());
        }
        return Result.ok("评论成功", "");
    }

    @Override
    public Result getComments(Long id, Long lastId, Integer offset) {
        // 从缓存中获取
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate
                .opsForZSet().reverseRangeByScoreWithScores(KeyConstant.COMMENT_KEY + id, 0, lastId, offset, Long.parseLong(systemConstants.defaultPageSize));
        return Result.ok(getScrollResult(typedTuples));
    }

    @Override
    public Result getTwoComments(Long postId, Long commentId) {
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId).eq(Comment::getParentId, commentId).orderByAsc(Comment::getCreateTime));
        if (comments == null || comments.isEmpty()) {
            return Result.ok(List.of());
        }
        return Result.ok(comments);
    }

    private ScrollResult<Comment> getScrollResult(Set<ZSetOperations.TypedTuple<Object>> typedTuples) {
        if (typedTuples == null || typedTuples.isEmpty()) {
            ScrollResult<Comment> objectScrollResult = new ScrollResult<>();
            objectScrollResult.setList(new ArrayList<>());
            objectScrollResult.setMinTime(System.currentTimeMillis());
            objectScrollResult.setOffset(0);
            return objectScrollResult;
        }
        List<Object> collect = typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getValue).toList();
        // 解析id
        List<Comment> comments = collect.stream()
                .map(comment -> BeanUtil.copyProperties(comment, Comment.class)).toList();
        // 获取 score
        List<Double> scores = typedTuples.stream()
                .map(ZSetOperations.TypedTuple::getScore).toList();
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
        ScrollResult<Comment> postVOScrollResult = new ScrollResult<>();
        postVOScrollResult.setList(comments);
        postVOScrollResult.setOffset(nweOffset);
        postVOScrollResult.setMinTime(scores.getLast().longValue());
        return postVOScrollResult;
    }
}

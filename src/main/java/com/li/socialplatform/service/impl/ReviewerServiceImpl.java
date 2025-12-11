package com.li.socialplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.mapper.CommentMapper;
import com.li.socialplatform.mapper.PostMapper;
import com.li.socialplatform.pojo.dto.SearchPostDTO;
import com.li.socialplatform.pojo.entity.Comment;
import com.li.socialplatform.pojo.entity.Post;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.pojo.vo.PostVO;
import com.li.socialplatform.service.IReviewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author e69d8e
 * @since 2025/12/10 14:01
 */
@Service
@RequiredArgsConstructor
public class ReviewerServiceImpl implements IReviewerService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final RedisTemplate<String, Object> redisTemplate;
//    private final UserMapper userMapper;

    @Override
    public Result banPost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error(MessageConstant.POST_NOT_EXIST);
        }
        post.setEnabled(!post.getEnabled());
        // 更新数据库
        return postMapper.updateById(post) > 0 ? Result.ok("封禁/解封成功", "") : Result.error("封禁/解封失败");
    }

    @Override
    public Result deleteComment(Long id, Long postId) {
        // 如果是一级评论 还要删除他的下一级评论
        Boolean delete = redisTemplate.delete(KeyConstant.COMMENT_KEY + postId + id);
        if (delete) {
            commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, id));
        }
        return commentMapper.deleteById(id) > 0 ? Result.ok("封禁/解封成功", "") : Result.error("封禁/解封失败");
    }

    @Override
    public Result listAllPost(SearchPostDTO searchPostDTO) {
        if (searchPostDTO.getPageNum() == null) {
            searchPostDTO.setPageNum(1);
        }
        if (searchPostDTO.getPageSize() == null) {
            searchPostDTO.setPageSize(10);
        }
        if (searchPostDTO.getSearch() == null) {
            searchPostDTO.setSearch("");
        }
        IPage<Post> page = new Page<>(searchPostDTO.getPageNum(), searchPostDTO.getPageSize());
        LambdaQueryWrapper<Post> search = new LambdaQueryWrapper<Post>()
                .like(Post::getContent, searchPostDTO.getSearch());
        if (searchPostDTO.getEnabled() != null) {
            search.eq(Post::getEnabled, searchPostDTO.getEnabled());
        }
        if (searchPostDTO.getCategoryId() != null) {
            search.eq(Post::getCategoryId, searchPostDTO.getCategoryId());
        }
        IPage<Post> postIPage = postMapper.selectPage(page, search);
        List<Post> records = postIPage.getRecords();
        if (records.isEmpty()) {
            return Result.ok(List.of(), postIPage.getTotal());
        }
        List<PostVO> postVOS = new ArrayList<>();
        for (Post record : records) {
            PostVO postVO = new PostVO();
            postVO.setPost(record);
//            if (record.getUserId() !=  null) {
//                postVO.setUser(getUserVO(record.getUserId()));
//            }
            postVOS.add(postVO);
        }
        return Result.ok(postVOS, postIPage.getTotal());
    }

//    private UserVO getUserVO(Long id) {
//        User user = userMapper.selectById(id);
//        UserVO userVO = new UserVO();
//        userVO.setId(user.getId());
//        userVO.setUsername(user.getUsername());
//        userVO.setNickname(user.getNickname());
//        userVO.setAvatar(user.getAvatar());
//        userVO.setBio(user.getBio());
//        userVO.setGender(user.getGender());
//        return userVO;
//    }
}

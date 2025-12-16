package com.li.socialplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.properties.SystemConstants;
import com.li.socialplatform.common.utils.UserIdUtil;
import com.li.socialplatform.mapper.AuthorityMapper;
import com.li.socialplatform.mapper.PostImageMapper;
import com.li.socialplatform.mapper.PostMapper;
import com.li.socialplatform.mapper.UserMapper;
import com.li.socialplatform.pojo.dto.UserDTO;
import com.li.socialplatform.pojo.entity.Post;
import com.li.socialplatform.pojo.entity.PostImage;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.pojo.entity.User;
import com.li.socialplatform.pojo.vo.PostImageVO;
import com.li.socialplatform.pojo.vo.PostVO;
import com.li.socialplatform.pojo.vo.UserVO;
import com.li.socialplatform.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author e69d8e
 * @since 2025/12/8 15:45
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserMapper userMapper;
    private final SystemConstants systemConstants;
    private final AuthorityMapper authorityMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final UserIdUtil userIdUtil;

    // 密码加密
    private String encodePassword(String password) {
        // 密码加密
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String result = encoder.encode(password);
        return "{bcrypt}" + result;
    }
    // 获取当前登录用户的用户名
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @Override
    public Result register(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUsername() == null || userDTO.getPassword() == null) {
            return Result.error(MessageConstant.USER_IS_EMPTY);
        }
        // 使用正则校验
        if (!userDTO.getUsername().matches("^[a-zA-Z0-9_-]{4,16}$")) {
            return Result.error(MessageConstant.USERNAME_FORMAT_ERROR);
        }
        if (!userDTO.getPassword().matches("^[a-zA-Z0-9_-]{6,16}$")) {
            return Result.error(MessageConstant.PASSWORD_FORMAT_ERROR);
        }
        // 判断是否已经有该用户名
        User existingUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userDTO.getUsername()));
        if (existingUser != null) {
            return Result.error(MessageConstant.USERNAME_ALREADY_EXISTS);
        }
        User user = new User();
        // 使用UUID生成随机昵称
        String uuid = UUID.randomUUID().toString().replace("-", "");
        user.setNickname(systemConstants.userNicknamePrefix + uuid);
        user.setPassword(encodePassword(userDTO.getPassword()));
        user.setUsername(userDTO.getUsername());
        user.setAvatar(systemConstants.defaultAvatar);
        userMapper.insert(user);
        return Result.ok(MessageConstant.REGISTER_SUCCESS, "");
    }



    @Override
    public Result getUserProfile(Long id) {
        User user;
        boolean followed = false;
        if (id == null) {
            // 查询登录用户的信息
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername()));
            if (user == null) {
                // 用户未登录
                throw new AccessDeniedException("用户未登录");
            }
            id = user.getId();
        } else {
            // 查询其他用户的信息
            user = userMapper.selectById(id);
            if (user == null) {
                return Result.error(MessageConstant.USER_NOT_FOUND);
            }
            // 查询当前用户有没有关注
            Long userId = userIdUtil.getUserId();
            if (userId != null) {
                Double score = redisTemplate.opsForZSet().score(KeyConstant.FOLLOW_LIST + userId, id);
                followed = score != null;
            }
        }
        // 获取角色
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        Integer count = (Integer) redisTemplate.opsForValue().get(KeyConstant.FOLLOW_COUNT_KEY + id);
        userVO.setCount(count == null ? 0 : count);
        userVO.setAuthority(authorityMapper.selectById(user.getAuthorityId()).getAuthority());
        userVO.setFollowed(followed);
        return Result.ok(userVO);
    }

    @Override
    public Result updateUserProfile(UserDTO userDTO) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername())
        );
        if (userDTO.getGender() == null || userDTO.getGender() < 0 || userDTO.getGender() > 2) {
            return Result.error(MessageConstant.USER_INFO_ERROR);
        }
        if (userDTO.getNickname() == null || userDTO.getNickname().isEmpty() || userDTO.getNickname().length() > Integer.parseInt(systemConstants.nicknameMaxLength)) {
            return Result.error(MessageConstant.NICKNAME_ERROR);
        }
        if (userDTO.getAvatar() == null || userDTO.getAvatar().isEmpty()) {
            return Result.error(MessageConstant.USER_INFO_ERROR);
        }
        if (userDTO.getBio() != null && userDTO.getBio().length() > Integer.parseInt(systemConstants.bioMaxLength)) {
            return Result.error(MessageConstant.BIO_ERROR);
        }
        if (user == null) {
            return Result.error(MessageConstant.USER_NOT_FOUND);
        }
        // 更新用户信息
        user.setNickname(userDTO.getNickname());
        user.setAvatar(userDTO.getAvatar() == null || userDTO.getAvatar().isEmpty() ? user.getAvatar() : userDTO.getAvatar());
        user.setBio(userDTO.getBio() == null ? user.getBio() : userDTO.getBio());
        user.setGender(userDTO.getGender() == null ? user.getGender() : userDTO.getGender());
        user.setFansPrivate(userDTO.getFansPrivate() == null ? user.getFansPrivate() : userDTO.getFansPrivate());
        user.setFollowPrivate(userDTO.getFollowPrivate() == null ? user.getFollowPrivate() : userDTO.getFollowPrivate());
        userMapper.updateById(user);
        return Result.ok("更新成功", "");
    }

    @Override
    public Result updatePassword(UserDTO userDTO) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, getCurrentUsername())
        );
        user.setPassword(encodePassword(userDTO.getPassword()));
        userMapper.updateById(user);
        return Result.ok("修改成功", "");
    }

    @Override
    public Result signIn() {
        LocalDateTime now = LocalDateTime.now();
        String key = KeyConstant.SIGN_IN_KEY + getCurrentUsername() + now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 获取今天是当月的第几天
        int day = now.getDayOfMonth();
        // 设置签到
        redisTemplate.opsForValue()
                .setBit(key, day - 1, true);
        // 计算当月截至今天连续签到次数
        List<Long> longs = redisTemplate.opsForValue().bitField(key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(day))
                        .valueAt(0) // 从第0位开始读取
        );
        // 0000000011
        Long tmp = null;
        if (longs != null) {
            tmp = longs.getFirst();
        }
        int count = 0;
        while (tmp != null && tmp != 0) {
            if ((tmp & 1) == 1) {
                count++;
            } else {
                break;
            }
            tmp = tmp >> 1;
        }
        return Result.ok(count);
    }

    @Override
    public Result signInCount() {
        LocalDateTime now = LocalDateTime.now();
        String key = KeyConstant.SIGN_IN_KEY + getCurrentUsername() + now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        List<Long> longs = redisTemplate.opsForValue().bitField(key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(now.getDayOfMonth()))
                        .valueAt(0) // 从第0位开始读取
        );
        Long tmp = null;
        if (longs != null) {
            tmp = longs.getFirst();
        }
        int count = 0;
        while (tmp != null && tmp != 0) {
            if ((tmp & 1) == 1) {
                count++;
            } else {
                break;
            }
            tmp = tmp >>> 1;
        }
        return Result.ok(count);
    }

    @Override
    public Result listPost(String searchContent, Integer pageNum, Integer pageSize, Integer categoryId) {
        IPage<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> search = new LambdaQueryWrapper<Post>()
                .and(wrapper -> wrapper
                        .like(Post::getContent, searchContent)
                        .or()
                        .like(Post::getTitle, searchContent)
                )
                .eq(Post::getEnabled, true);
        if (categoryId != null) {
            search = search.eq(Post::getCategoryId, categoryId);
        }
        IPage<Post> postIPage = postMapper.selectPage(page, search);
        List<Post> records = postIPage.getRecords();
        if (records.isEmpty()) {
            return Result.ok(List.of(), postIPage.getTotal());
        }
        List<PostVO> postVOS = new ArrayList<>();
        Long userId = userIdUtil.getUserId();
        for (Post record : records) {
            List<PostImage> postImages = postImageMapper.selectList(
                    new LambdaQueryWrapper<PostImage>().eq(PostImage::getPostId, record.getId()));
            PostVO postVO = BeanUtil.copyProperties(record, PostVO.class);
            postVO.setImgUrl(getImgUrl(postImages));
            postVO.setPostImages(postImagesToPostImagesVOs(postImages));
            if (userId == null) {
                postVO.setLiked(false);
            } else {
                postVO.setLiked(
                        redisTemplate.opsForSet()
                                .isMember(KeyConstant.LIKE_KEY + record.getId(), userId));
            }
            Integer count = (Integer) redisTemplate.opsForValue().get(KeyConstant.LIKE_COUNT + record.getId());
            postVO.setCount(count == null ? 0 : count);
            postVOS.add(postVO);
        }
        return Result.ok(postVOS, postIPage.getTotal());
    }

    @Override
    public Result listUser(String nickname, Integer pageNum, Integer pageSize, Integer gender) {
        IPage<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> search = new LambdaQueryWrapper<User>()
                .like(User::getNickname, nickname);
        if (gender != null) {
            search.eq(User::getGender, gender);
        }
        IPage<User> userIPage = userMapper.selectPage(page, search);
        List<User> records = userIPage.getRecords();
        List<UserVO> users = new ArrayList<>();
        for (User record : records) {
            UserVO userVO = BeanUtil.copyProperties(record, UserVO.class);
            userVO.setAuthority(authorityMapper.selectById(record.getAuthorityId()).getAuthority());
            Double score = redisTemplate.opsForZSet().score(KeyConstant.FOLLOW_LIST + userIdUtil.getUserId(), record.getId());
            userVO.setFollowed(score != null);
            Integer count = (Integer) redisTemplate.opsForValue().get(KeyConstant.FOLLOW_COUNT_KEY + record.getId());
            userVO.setCount(count == null ? 0 : count);
            users.add(userVO);
        }
        return Result.ok(users, userIPage.getTotal());
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

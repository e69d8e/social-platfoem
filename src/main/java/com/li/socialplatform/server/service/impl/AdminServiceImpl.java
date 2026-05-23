package com.li.socialplatform.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.li.socialplatform.common.constant.AuthorityConstant;
import com.li.socialplatform.common.constant.MessageConstant;
import com.li.socialplatform.common.utils.BanCacheUtil;
import com.li.socialplatform.common.utils.DataCacheUtil;
import com.li.socialplatform.common.utils.UserIdUtil;
import com.li.socialplatform.server.mapper.UserMapper;
import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.pojo.entity.User;
import com.li.socialplatform.pojo.vo.UserVO;
import com.li.socialplatform.server.service.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * @author e69d8e
 * @since 2025/12/10 14:04
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final UserMapper userMapper;
    private final UserIdUtil userIdUtil;
    private final DataCacheUtil dataCacheUtil;
    private final BanCacheUtil banCacheUtil;

    @Override
    public Result banUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(MessageConstant.USER_NOT_EXIST);
        }
        if (user.getEnabled()) {
            banCacheUtil.addBanUser(userIdUtil.getUserId(), id);
        } else {
            banCacheUtil.removeBanUser(id);
        }
        user.setEnabled(!user.getEnabled());
        return userMapper.updateById(user) > 0 ? Result.ok(MessageConstant.BAN_SUCCESS, "") : Result.error(MessageConstant.BAN_FAIL);
    }

    @Override
    public Result getBanUser(Integer pageNum, Integer pageSize) {
        long start = ((long) (pageNum - 1) * pageSize);
        long end = start + pageSize - 1;
        Long total = banCacheUtil.getBanUserTotal();
        if (total == null || total == 0) {
            return Result.ok(List.of(), 0L);
        }
        if (start > total) {
            return Result.ok(List.of(), 0L);
        }
        if (end > total) {
            end = total - 1;
        }
        Set<Object> members = banCacheUtil.getBanUserIds(start, end);
        if (members == null || members.isEmpty()) {
            return Result.ok(List.of(), 0L);
        }
        List<Long> ids = members.stream().map(member -> Long.valueOf(member.toString())).toList();
        List<UserVO> users = new ArrayList<>();
        for (Long id : ids) {
            User user = userMapper.selectById(id);
            UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
            userVO.setEnabled(false);
            Long currentUserId = userIdUtil.getUserId();
            userVO.setFollowed(currentUserId != null && dataCacheUtil.isFollowed(currentUserId, id));
            userVO.setFansCount(dataCacheUtil.getFollowerCount(user.getId()));
            users.add(userVO);
        }
        return Result.ok(users, total);
    }

    @Override
    public Result setReviewer(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(MessageConstant.USER_NOT_EXIST);
        }
        user.setAuthorityId(AuthorityConstant.REVIEWER);
        return userMapper.updateById(user) > 0 ? Result.ok(MessageConstant.SET_SUCCESS, "") : Result.error(MessageConstant.SET_FAIL);
    }

    @Override
    public Result setUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(MessageConstant.USER_NOT_EXIST);
        }
        user.setAuthorityId(AuthorityConstant.USER);
        return userMapper.updateById(user) > 0 ? Result.ok(MessageConstant.SET_SUCCESS, "") : Result.error(MessageConstant.SET_FAIL);
    }
}

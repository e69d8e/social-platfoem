package com.li.socialplatform.common.constant;

/**
 * @author e69d8e
 * @since 2025/12/8 16:15
 */
public class MessageConstant {
    public static final String USER_IS_EMPTY  = "用户名和密码不能为空";
    public static final String USERNAME_ALREADY_EXISTS = "该用户已经存在";
    public static final String USERNAME_FORMAT_ERROR = "用户名格式错误";
    public static final String PASSWORD_FORMAT_ERROR = "密码格式错误";

    // 异常
    public static final String EXCEPTION = "服务器异常";
    public static final String MethodArgumentNotValidException_CODE = "1001";
    public static final String BizException_CODE = "1002";
    public static final String ID_IS_NULL = "ID为空";
    public static final String USER_IS_FOLLOWED = "用户已经关注了";
    public static final String USER_NOT_EXIST = "用户不存在";
    public static final String USER_CANNOT_FOLLOW_SELF = "无法关注自己";
    public static final String USER_NOT_FOLLOWED = "用户未关注";
    public static final String USER_FANS_PRIVATE = "该用户粉丝列表为私密状态";
    public static final String USER_FOLLOW_PRIVATE = "该用户关注列表为私密状态";
    public static final String CONTENT_IS_NULL = "内容不能为空";
    public static final String USER_NOT_FOUND = "用户不存在";
    public static final String POST_NOT_EXIST = "该帖子不存在";
    public static final String TITLE_IS_NULL = "标题不能为空";
}

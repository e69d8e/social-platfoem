package com.li.socialplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.li.socialplatform.pojo.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}

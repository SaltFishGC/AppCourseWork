package com.myapp.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myapp.server.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UsersMapper extends BaseMapper<User> {
    // 可以添加自定义的SQL方法
}
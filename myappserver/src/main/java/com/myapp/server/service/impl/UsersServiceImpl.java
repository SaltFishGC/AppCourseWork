package com.myapp.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myapp.server.entity.User;
import com.myapp.server.mapper.UsersMapper;
import com.myapp.server.service.IUsersService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, User> implements IUsersService {
    // 实现自定义的业务方法
}

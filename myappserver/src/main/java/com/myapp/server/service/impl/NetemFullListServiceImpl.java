package com.myapp.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myapp.server.entity.NetemFullList;
import com.myapp.server.mapper.NetemFullListMapper;
import com.myapp.server.service.INetemFullListService;
import org.springframework.stereotype.Service;

/**
 * Netem完整列表服务实现类
 */
@Service
public class NetemFullListServiceImpl extends ServiceImpl<NetemFullListMapper, NetemFullList> implements INetemFullListService {
    // 实现自定义的业务方法
}

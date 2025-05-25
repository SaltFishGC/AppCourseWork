package com.myapp.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myapp.server.entity.NetemLearnedDetail;
import com.myapp.server.mapper.NetemLearnedDetailMapper;
import com.myapp.server.service.INetemLearnedDetailService;
import org.springframework.stereotype.Service;

/**
 * Netem学习详情服务实现类
 */
@Service
public class NetemLearnedDetailServiceImpl extends ServiceImpl<NetemLearnedDetailMapper, NetemLearnedDetail> implements INetemLearnedDetailService {
    // 实现自定义的业务方法
}

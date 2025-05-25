package com.myapp.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myapp.server.entity.TimeLearned;
import com.myapp.server.mapper.TimeLearnedMapper;
import com.myapp.server.service.ITimeLearnedService;
import org.springframework.stereotype.Service;

/**
 * 学习时间服务实现类
 */
@Service
public class TimeLearnedServiceImpl extends ServiceImpl<TimeLearnedMapper, TimeLearned> implements ITimeLearnedService {
    // 实现自定义的业务方法
}

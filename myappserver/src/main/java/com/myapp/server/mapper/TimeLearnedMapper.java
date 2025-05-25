package com.myapp.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myapp.server.entity.TimeLearned;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习时间Mapper接口
 */
@Mapper
public interface TimeLearnedMapper extends BaseMapper<TimeLearned> {
    // 可以添加自定义的SQL方法
}

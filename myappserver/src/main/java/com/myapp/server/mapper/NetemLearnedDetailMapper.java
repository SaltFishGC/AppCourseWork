package com.myapp.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myapp.server.entity.NetemLearnedDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * Netem学习详情Mapper接口
 */
@Mapper
public interface NetemLearnedDetailMapper extends BaseMapper<NetemLearnedDetail> {
    // 可以添加自定义的SQL方法
}

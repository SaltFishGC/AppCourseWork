package com.myapp.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myapp.server.entity.NetemFullList;
import org.apache.ibatis.annotations.Mapper;

/**
 * Netem完整列表Mapper接口
 */
@Mapper
public interface NetemFullListMapper extends BaseMapper<NetemFullList> {
    // 可以添加自定义的SQL方法
}

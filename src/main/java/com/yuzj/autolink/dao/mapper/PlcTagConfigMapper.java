package com.yuzj.autolink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuzj.autolink.dao.model.PlcTagConfig;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * PLC标签配置Mapper接口
 */
@Mapper
@Repository
public interface PlcTagConfigMapper extends BaseMapper<PlcTagConfig> {
}

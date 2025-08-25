package com.yuzj.autolink.dao.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * PLC标签配置实体类
 * 对应表: plc_tag_configs
 * @author yuzj002
 */
@Data
@TableName("plc_tag_config")
public class PlcTagConfig {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 标签名称
     */
    private String tagName;
    
    /**
     * PLC地址
     */
    private String address;
    
    /**
     * 数据类型
     */
    private String dataType;
    
    /**
     * 标签描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

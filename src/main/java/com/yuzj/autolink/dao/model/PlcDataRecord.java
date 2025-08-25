package com.yuzj.autolink.dao.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PLC数据记录实体类
 * 对应表: plc_data_records
 *
 * @author yuzj002
 */
@Data
@TableName("plc_data_record")
public class PlcDataRecord {

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
     * 数据值
     */
    private String tagValue;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 数据质量
     */
    private String quality;

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

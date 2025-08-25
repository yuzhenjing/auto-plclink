package com.yuzj.autolink.dao.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报警记录实体类
 * 对应表: alarm_records
 *
 * @author yuzj002
 */
@Data
@TableName("alarm_record")
public class AlarmRecord {

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
     * 报警类型
     */
    private String alarmType;

    /**
     * 报警信息
     */
    private String alarmMessage;

    /**
     * 报警级别
     */
    private String alarmLevel;

    /**
     * 报警时的数据值
     */
    private String alarmValue;

    /**
     * 报警时间
     */
    private LocalDateTime timestamp;
    /**
     * 是否已确认
     */
    private Boolean isAcknowledged;

    /**
     * 确认时间
     */
    private LocalDateTime acknowledgedTime;
}

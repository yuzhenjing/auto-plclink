package com.yuzj.autolink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * PLC连接配置属性
 * 用于配置PLC设备的连接参数
 *
 * @author yuzj002
 */
@Data
@Component
@ConfigurationProperties(prefix = "plc.connection")
public class PlcProperties {

    /**
     * 通信协议类型
     */
    private String protocol = "S7";

    /**
     * PLC设备IP地址
     */
    private String host = "192.168.0.1";

    /**
     * PLC端口号
     */
    private int port = 102;

    /**
     * PLC机架号
     */
    private int rack = 0;

    /**
     * PLC插槽号
     */
    private int slot = 1;

    /**
     * 连接超时时间(毫秒)
     */
    private int timeout = 5000;

    /**
     * 重试次数
     */
    private int retryCount = 3;

    /**
     * 重试间隔时间(毫秒)
     */
    private int retryInterval = 1000;
}

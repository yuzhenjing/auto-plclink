package com.yuzj.autolink.plc.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PLC连接状态事件
 * 用于在控制器之间传递连接状态变化信息
 *
 * @author yuzj002
 */
@Getter
@AllArgsConstructor
public class PlcConnectStatusEvent {
    /**
     * 连接状态标志
     */
    private boolean connected;
}

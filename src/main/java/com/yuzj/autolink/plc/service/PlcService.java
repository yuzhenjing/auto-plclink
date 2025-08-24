package com.yuzj.autolink.plc.service;

import com.yuzj.autolink.exception.PlcConnectionException;
import com.yuzj.autolink.exception.PlcReadException;
import com.yuzj.autolink.exception.PlcWriteException;

import java.util.Map;

/**
 * PLC通信服务接口
 * 提供PLC设备的连接、断开连接、读写等操作
 *
 * @author yuzj002
 */
public interface PlcService {

    /**
     * 建立PLC连接
     *
     * @throws PlcConnectionException 连接异常
     */
    void connect() throws PlcConnectionException;

    /**
     * 断开PLC连接
     */
    void disconnect();

    /**
     * 检查PLC连接状态
     *
     * @return 是否已连接
     */
    boolean isConnected();

    /**
     * 读取单个地址数据
     *
     * @param address 地址
     * @return 读取的数据
     * @throws PlcReadException 读取异常
     */
    Object read(String address) throws PlcReadException;

    /**
     * 批量读取多个地址数据
     *
     * @param addresses 地址数组
     * @return 地址与数据的映射
     * @throws PlcReadException 读取异常
     */
    Map<String, Object> readMultiple(String[] addresses) throws PlcReadException;

    /**
     * 写入单个地址数据
     *
     * @param address 地址
     * @param value   值
     * @throws PlcWriteException 写入异常
     */
    void write(String address, Object value) throws PlcWriteException;

    /**
     * 批量写入多个地址数据
     *
     * @param values 地址与值的映射
     * @throws PlcWriteException 写入异常
     */
    void writeMultiple(Map<String, Object> values) throws PlcWriteException;
    
}

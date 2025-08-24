package com.yuzj.autolink.plc.service.impl;

import com.yuzj.autolink.config.PlcProperties;
import com.yuzj.autolink.exception.PlcConnectionException;
import com.yuzj.autolink.exception.PlcReadException;
import com.yuzj.autolink.exception.PlcWriteException;
import com.yuzj.autolink.plc.service.PlcService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author yuzj002
 */
@Service
public class ModbusRtuOperatorService implements PlcService {

    private PlcProperties config;
    private boolean connected = false;

    @Override
    public void connect() throws PlcConnectionException {
        // 实现Modbus RTU连接逻辑
        try {
            // 模拟连接过程
            Thread.sleep(1000);
            connected = true;
        } catch (InterruptedException e) {
            throw new PlcConnectionException("Modbus RTU连接失败", e);
        }
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public Object read(String address) throws PlcReadException {
        // 实现Modbus RTU读取逻辑
        if (!isConnected()) {
            throw new PlcReadException("PLC未连接");
        }

        // 模拟读取过程
        try {
            Thread.sleep(100); // RTU通常比TCP慢
        } catch (InterruptedException e) {
            throw new PlcReadException("读取被中断", e);
        }

        // 返回模拟数据
        if (address.startsWith("4")) {
            return (int) (Math.random() * 100); // 模拟保持寄存器值
        } else {
            return Math.random() > 0.5; // 模拟线圈状态
        }
    }

    @Override
    public Map<String, Object> readMultiple(String[] addresses) throws PlcReadException {
        // 实现批量读取
        return null;
    }

    @Override
    public void write(String address, Object value) throws PlcWriteException {
        // 实现Modbus RTU写入逻辑
        if (!isConnected()) {
            throw new PlcWriteException("PLC未连接");
        }

        // 模拟写入过程
        try {
            Thread.sleep(100); // RTU通常比TCP慢
        } catch (InterruptedException e) {
            throw new PlcWriteException("写入被中断", e);
        }

        System.out.println("写入Modbus RTU: " + address + " = " + value);
    }

    @Override
    public void writeMultiple(Map<String, Object> values) throws PlcWriteException {
        // 实现批量写入
    }

    @Override
    public PlcProperties getConfig() {
        return config;
    }

    @Override
    public void setConfig(PlcProperties config) {
        this.config = config;
    }
}
package com.yuzj.autolink.plc.service.impl;

import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.S7Serializer;
import com.github.s7connector.api.factory.S7ConnectorFactory;
import com.github.s7connector.api.factory.S7SerializerFactory;
import com.yuzj.autolink.config.PlcProperties;
import com.yuzj.autolink.exception.PlcConnectionException;
import com.yuzj.autolink.exception.PlcReadException;
import com.yuzj.autolink.exception.PlcWriteException;
import com.yuzj.autolink.plc.service.PlcService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class S7PlcOperatorService implements PlcService {

    private PlcProperties config;
    private S7Connector connector;
    private S7Serializer serializer;

    @Override
    public void connect() throws PlcConnectionException {
        try {
            connector = S7ConnectorFactory
                    .buildTCPConnector()
                    .withHost(config.getHost())
                    .withPort(config.getPort())
                    .withRack(config.getRack())
                    .withSlot(config.getSlot())
                    .withTimeout(config.getTimeout())
                    .build();

            serializer = S7SerializerFactory.buildSerializer(connector);
        } catch (Exception e) {
            throw new PlcConnectionException("连接S7 PLC失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        if (connector != null) {
            try {
                connector.close();
            } catch (Exception e) {
                // 记录日志但不要抛出异常
                System.err.println("关闭S7连接时出错: " + e.getMessage());
            } finally {
                connector = null;
                serializer = null;
            }
        }
    }

    @Override
    public boolean isConnected() {
        return connector != null;
    }

    @Override
    public Object read(String address) throws PlcReadException {
        if (!isConnected()) {
            throw new PlcReadException("PLC未连接");
        }

        try {
            // 解析地址格式: DB10.DBX0.0 (数据块10，字节0，位0)
            if (address.startsWith("DB")) {
                return readDataBlock(address);
            } else {
                return readMemoryArea(address);
            }
        } catch (Exception e) {
            throw new PlcReadException("读取S7 PLC数据失败: " + e.getMessage(), e);
        }
    }

    private Object readDataBlock(String address) throws Exception {
        String[] parts = address.split("\\.");
        int dbNumber = Integer.parseInt(parts[0].substring(2));

        if (parts[1].startsWith("DBX")) {
            // 读取位
            int byteOffset = Integer.parseInt(parts[1].substring(3));
            int bitOffset = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            return serializer.dispense(Boolean.class, dbNumber, byteOffset, bitOffset);
        } else if (parts[1].startsWith("DBB")) {
            // 读取字节
            int byteOffset = Integer.parseInt(parts[1].substring(3));
            return serializer.dispense(Byte.class, dbNumber, byteOffset);
        } else if (parts[1].startsWith("DBW")) {
            // 读取字
            int byteOffset = Integer.parseInt(parts[1].substring(3));
            return serializer.dispense(Short.class, dbNumber, byteOffset);
        } else if (parts[1].startsWith("DBD")) {
            // 读取双字
            int byteOffset = Integer.parseInt(parts[1].substring(3));
            return serializer.dispense(Integer.class, dbNumber, byteOffset);
        } else if (parts[1].startsWith("DBR")) {
            // 读取浮点数
            int byteOffset = Integer.parseInt(parts[1].substring(3));
            return serializer.dispense(Float.class, dbNumber, byteOffset);
        } else {
            throw new PlcReadException("不支持的S7地址格式: " + address);
        }
    }

    private Object readMemoryArea(String address) throws Exception {
        // 处理I、Q、M等存储区
        // 实现略，类似readDataBlock
        throw new PlcReadException("暂不支持非DB区地址: " + address);
    }

    @Override
    public Map<String, Object> readMultiple(String[] addresses) throws PlcReadException {
        Map<String, Object> results = new HashMap<>();
        for (String address : addresses) {
            results.put(address, read(address));
        }
        return results;
    }

    @Override
    public void write(String address, Object value) throws PlcWriteException {
        if (!isConnected()) {
            throw new PlcWriteException("PLC未连接");
        }

        try {
            // 解析地址格式并写入
            if (address.startsWith("DB")) {
                writeDataBlock(address, value);
            } else {
                writeMemoryArea(address, value);
            }
        } catch (Exception e) {
            throw new PlcWriteException("写入S7 PLC数据失败: " + e.getMessage(), e);
        }
    }

    private void writeDataBlock(String address, Object value) throws Exception {
        String[] parts = address.split("\\.");
        int dbNumber = Integer.parseInt(parts[0].substring(2));

        if (parts[1].startsWith("DBX")) {
            // 写入位
            int byteOffset = Integer.parseInt(parts[1].substring(3));
            int bitOffset = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            serializer.store(value, byteOffset, bitOffset);
        }
    }

    private void writeMemoryArea(String address, Object value) throws Exception {
        // 处理I、Q、M等存储区
        // 实现略，类似writeDataBlock
        throw new PlcWriteException("暂不支持非DB区地址: " + address);
    }

    @Override
    public void writeMultiple(Map<String, Object> values) throws PlcWriteException {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            write(entry.getKey(), entry.getValue());
        }
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
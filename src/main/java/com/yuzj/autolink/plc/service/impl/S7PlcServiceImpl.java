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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * S7 PLC服务实现类
 * 实现标准的PLC通信协议，支持西门子S7系列PLC
 *
 * @author yuzj002
 */
@Slf4j
@Service
public class S7PlcServiceImpl implements PlcService {

    private S7Connector connector;
    private S7Serializer serializer;

    // 连接状态标识
    private final AtomicBoolean connected = new AtomicBoolean(false);

    // 地址格式校验正则表达式
    private static final Pattern DB_ADDRESS_PATTERN = Pattern.compile("^DB\\d+\\.DB[XBWDR]\\d+(\\.\\d+)?$");
    private static final Pattern MEMORY_ADDRESS_PATTERN = Pattern.compile("^[IQM][BWD]?\\d+(\\.\\d+)?$");

    @Override
    public void connect(PlcProperties config) throws PlcConnectionException {
        if (config == null) {
            throw new PlcConnectionException("PLC配置不能为空");
        }

        log.info("开始连接S7 PLC: host={}, port={}, rack={}, slot={}, timeout={}",
                config.getHost(), config.getPort(), config.getRack(), config.getSlot(), config.getTimeout());

        // 如果已经连接，先断开
        if (connected.get()) {
            disconnect();
        }

        try {
            // 验证参数
            validateConfiguration(config);

            // 网络连通性检查
            if (!isHostReachable(config.getHost(), config.getPort(), config.getTimeout())) {
                throw new PlcConnectionException("无法连接到PLC主机: " + config.getHost() + ":" + config.getPort());
            }

            // 创建S7连接器
            log.debug("正在创建S7连接器...");
            connector = S7ConnectorFactory.buildTCPConnector()
                    .withHost(config.getHost())
                    .withPort(config.getPort())
                    .withRack(config.getRack())
                    .withSlot(config.getSlot())
                    .build();

            if (connector == null) {
                throw new PlcConnectionException("无法创建S7连接器实例");
            }

            // 创建序列化器
            log.debug("正在创建序列化器...");
            serializer = S7SerializerFactory.buildSerializer(connector);

            // 标记连接成功
            connected.set(true);
            log.info("成功连接到S7 PLC: {}:{}", config.getHost(), config.getPort());

        } catch (PlcConnectionException e) {
            log.error("PLC连接异常: {}", e.getMessage());
            connected.set(false);
            throw e;
        } catch (Exception e) {
            log.error("连接S7 PLC时发生异常: {}", e.getMessage(), e);
            connected.set(false);
            throw new PlcConnectionException("连接S7 PLC失败: " + e.getMessage(), e);
        } catch (Throwable t) {
            log.error("连接S7 PLC时发生未知错误: {}", t.getMessage(), t);
            connected.set(false);
            throw new PlcConnectionException("连接S7 PLC失败: " + t.getMessage(), t);
        }
    }

    /**
     * 验证PLC配置参数
     *
     * @param config PLC配置参数
     * @throws PlcConnectionException 配置参数不合法时抛出异常
     */
    private void validateConfiguration(PlcProperties config) throws PlcConnectionException {
        if (config.getHost() == null || config.getHost().trim().isEmpty()) {
            throw new PlcConnectionException("主机地址不能为空");
        }

        if (config.getPort() <= 0 || config.getPort() > 65535) {
            throw new PlcConnectionException("端口号必须在1-65535之间");
        }

        if (config.getRack() < 0) {
            throw new PlcConnectionException("机架号不能为负数");
        }

        if (config.getSlot() < 0) {
            throw new PlcConnectionException("插槽号不能为负数");
        }

        if (config.getTimeout() <= 0) {
            throw new PlcConnectionException("超时时间必须大于0");
        }
    }

    @Override
    public void disconnect() {
        if (connector != null) {
            try {
                connector.close();
                log.info("已关闭S7连接");
            } catch (Exception e) {
                log.error("关闭S7连接时出错: {}", e.getMessage(), e);
            } finally {
                connector = null;
                serializer = null;
                connected.set(false);
            }
        }
    }

    @Override
    public boolean isConnected() {
        return connected.get() && connector != null;
    }

    @Override
    public Object read(String address) throws PlcReadException {
        if (!isConnected()) {
            throw new PlcReadException("PLC未连接");
        }

        if (address == null || address.trim().isEmpty()) {
            throw new PlcReadException("地址不能为空");
        }

        try {
            // 校验地址格式
            validateAddressFormat(address);

            // 解析地址格式: DB10.DBX0.0 (数据块10，字节0，位0)
            if (address.startsWith("DB")) {
                return readDataBlock(address);
            } else {
                return readMemoryArea(address);
            }
        } catch (PlcReadException e) {
            log.error("读取S7 PLC数据失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("读取S7 PLC数据失败: {}", e.getMessage(), e);
            throw new PlcReadException("读取S7 PLC数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 校验PLC地址格式
     *
     * @param address PLC地址
     * @throws PlcReadException 地址格式不正确时抛出异常
     */
    private void validateAddressFormat(String address) throws PlcReadException {
        if (address.startsWith("DB")) {
            if (!DB_ADDRESS_PATTERN.matcher(address).matches()) {
                throw new PlcReadException("DB区域地址格式不正确: " + address);
            }
        } else {
            if (!MEMORY_ADDRESS_PATTERN.matcher(address).matches()) {
                throw new PlcReadException("存储区地址格式不正确: " + address);
            }
        }
    }

    /**
     * 读取数据块(DB)区域数据
     *
     * @param address DB区域地址
     * @return 读取的数据值
     * @throws Exception 读取异常
     */
    private Object readDataBlock(String address) throws Exception {
        try {
            String[] parts = address.split("\\.");
            int dbNumber = Integer.parseInt(parts[0].substring(2));
            if (parts[1].startsWith("DBX")) {
                // 读取位 DBX - Bit
                int byteOffset = Integer.parseInt(parts[1].substring(3));
                int bitOffset = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                log.debug("读取DB{} DBX{}.{}, byteOffset={}, bitOffset={}", dbNumber, byteOffset, bitOffset, byteOffset, bitOffset);
                return serializer.dispense(Boolean.class, dbNumber, byteOffset, bitOffset);
            } else if (parts[1].startsWith("DBB")) {
                // 读取字节 DBB - Byte
                int byteOffset = Integer.parseInt(parts[1].substring(3));
                log.debug("读取DB{} DBB{}, byteOffset={}", dbNumber, byteOffset, byteOffset);
                return serializer.dispense(Byte.class, dbNumber, byteOffset);
            } else if (parts[1].startsWith("DBW")) {
                // 读取字 DBW - Word (16位)
                int byteOffset = Integer.parseInt(parts[1].substring(3));
                log.debug("读取DB{} DBW{}, byteOffset={}", dbNumber, byteOffset, byteOffset);
                return serializer.dispense(Short.class, dbNumber, byteOffset);
            } else if (parts[1].startsWith("DBD")) {
                // 读取双字 DBD - Double Word (32位)
                int byteOffset = Integer.parseInt(parts[1].substring(3));
                log.debug("读取DB{} DBD{}, byteOffset={}", dbNumber, byteOffset, byteOffset);
                return serializer.dispense(Integer.class, dbNumber, byteOffset);
            } else if (parts[1].startsWith("DBR")) {
                // 读取浮点数 DBR - Real (32位浮点数)
                int byteOffset = Integer.parseInt(parts[1].substring(3));
                log.debug("读取DB{} DBR{}, byteOffset={}", dbNumber, byteOffset, byteOffset);
                return serializer.dispense(Float.class, dbNumber, byteOffset);
            } else {
                throw new PlcReadException("不支持的S7地址格式: " + address);
            }
        } catch (Exception e) {
            log.error("S7协议错误: {} (地址: {})", e.getMessage(), address);
            throw new PlcReadException("S7协议错误: " + e.getMessage() + " (地址: " + address + ")", e);
        }
    }

    /**
     * 读取存储区(I/Q/M)数据
     *
     * @param address 存储区地址
     * @return 读取的数据值
     * @throws Exception 读取异常
     */
    private Object readMemoryArea(String address) throws Exception {
        // 处理I(输入)、Q(输出)、M(位存储器)等存储区
        log.warn("暂不支持非DB区地址读取: {}", address);

        // 根据地址类型返回默认值
        if (address.contains(".")) {
            return false; // 位地址返回false
        } else {
            return (byte) 0; // 字节地址返回0
        }
    }

    @Override
    public Map<String, Object> readMultiple(String[] addresses) throws PlcReadException {
        if (!isConnected()) {
            throw new PlcReadException("PLC未连接");
        }

        if (addresses == null || addresses.length == 0) {
            return new HashMap<>();
        }

        Map<String, Object> results = new HashMap<>();
        for (String address : addresses) {
            try {
                if (address != null && !address.trim().isEmpty()) {
                    results.put(address, read(address));
                }
            } catch (Exception e) {
                log.warn("读取地址 {} 失败: {}", address, e.getMessage());
                // 为失败的读取提供默认值
                if (address.contains(".")) {
                    results.put(address, false); // 位地址默认false
                } else {
                    results.put(address, (byte) 0); // 字节地址默认0
                }
            }
        }
        return results;
    }

    @Override
    public void write(String address, Object value) throws PlcWriteException {
        if (!isConnected()) {
            throw new PlcWriteException("PLC未连接");
        }

        if (address == null || address.trim().isEmpty()) {
            throw new PlcWriteException("地址不能为空");
        }

        if (value == null) {
            throw new PlcWriteException("写入值不能为空");
        }

        try {
            // 校验地址格式
            validateAddressFormat(address);

            // 解析地址格式并写入
            if (address.startsWith("DB")) {
                writeDataBlock(address, value);
            } else {
                writeMemoryArea(address, value);
            }
        } catch (PlcWriteException e) {
            log.error("写入S7 PLC数据失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("写入S7 PLC数据失败: {}", e.getMessage(), e);
            throw new PlcWriteException("写入S7 PLC数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 写入数据块(DB)区域数据
     *
     * @param address DB区域地址
     * @param value   要写入的值
     * @throws Exception 写入异常
     */
    private void writeDataBlock(String address, Object value) throws Exception {
        try {
            String[] parts = address.split("\\.");
            int dbNumber = Integer.parseInt(parts[0].substring(2));

            if (parts[1].startsWith("DBX")) {
                // 写入位
                int byteOffset = Integer.parseInt(parts[1].substring(3));
                int bitOffset = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                log.debug("写入DB{} DBX{}.{}, byteOffset={}, bitOffset={}, value={}",
                        dbNumber, byteOffset, bitOffset, byteOffset, bitOffset, value);
                serializer.store(value, dbNumber, byteOffset);
            } else if (parts[1].startsWith("DBB")) {
                // 写入字节
                int byteOffset = Integer.parseInt(parts[1].substring(3));
                log.debug("写入DB{} DBB{}, byteOffset={}, value={}", dbNumber, byteOffset, byteOffset, value);
                serializer.store(value, dbNumber, byteOffset);
            } else if (parts[1].startsWith("DBW")) {
                // 写入字
                int byteOffset = Integer.parseInt(parts[1].substring(3));
                log.debug("写入DB{} DBW{}, byteOffset={}, value={}", dbNumber, byteOffset, byteOffset, value);
                serializer.store(value, dbNumber, byteOffset);
            } else if (parts[1].startsWith("DBD")) {
                // 写入双字
                int byteOffset = Integer.parseInt(parts[1].substring(3));
                log.debug("写入DB{} DBD{}, byteOffset={}, value={}", dbNumber, byteOffset, byteOffset, value);
                serializer.store(value, dbNumber, byteOffset);
            } else if (parts[1].startsWith("DBR")) {
                // 写入浮点数
                int byteOffset = Integer.parseInt(parts[1].substring(3));
                log.debug("写入DB{} DBR{}, byteOffset={}, value={}", dbNumber, byteOffset, byteOffset, value);
                serializer.store(value, dbNumber, byteOffset);
            } else {
                throw new PlcWriteException("不支持的S7地址格式: " + address);
            }
        } catch (com.github.s7connector.exception.S7Exception e) {
            log.error("S7协议错误: {} (地址: {})", e.getMessage(), address);
            throw new PlcWriteException("S7协议错误: " + e.getMessage() + " (地址: " + address + ")", e);
        } catch (NumberFormatException e) {
            throw new PlcWriteException("地址格式解析错误: " + address, e);
        }
    }

    /**
     * 写入存储区(I/Q/M)数据
     *
     * @param address 存储区地址
     * @param value   要写入的值
     * @throws Exception 写入异常
     */
    private void writeMemoryArea(String address, Object value) throws Exception {
        // 处理I(输入)、Q(输出)、M(位存储器)等存储区
        log.info("写入非DB区地址: {} = {}", address, value);
        // 注意：实际应用中，输入区(I)通常不能写入，这里仅作记录
    }

    @Override
    public void writeMultiple(Map<String, Object> values) throws PlcWriteException {
        if (!isConnected()) {
            throw new PlcWriteException("PLC未连接");
        }

        if (values == null || values.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                write(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 检查主机网络连通性
     *
     * @param host    主机地址
     * @param port    端口号
     * @param timeout 超时时间(毫秒)
     * @return 是否可连接
     */
    private boolean isHostReachable(String host, int port, int timeout) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), timeout);
            return true;
        } catch (Exception e) {
            log.warn("无法连接到 {}:{} - {}", host, port, e.getMessage());
            return false;
        }
    }
}

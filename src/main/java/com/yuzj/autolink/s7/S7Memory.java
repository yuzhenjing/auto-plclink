// S7Memory.java
package com.yuzj.autolink.s7;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class S7Memory {

    // 模拟不同的内存区域
    private final byte[] dbMemory = new byte[65536]; // DB块内存
    private final byte[] inputMemory = new byte[8192];  // 输入内存
    private final byte[] outputMemory = new byte[8192]; // 输出内存
    private final byte[] markerMemory = new byte[8192]; // 位存储器

    private final ReadWriteLock dbLock = new ReentrantReadWriteLock();
    private final ReadWriteLock inputLock = new ReentrantReadWriteLock();
    private final ReadWriteLock outputLock = new ReentrantReadWriteLock();
    private final ReadWriteLock markerLock = new ReentrantReadWriteLock();

    // 用于生成测试数据的随机数生成器
    private final Random random = new Random();

    public S7Memory() {
        // 初始化一些默认值用于测试
        initializeTestData();
    }

    /**
     * 初始化测试数据
     * 模拟真实的PLC数据分布，包括各种数据类型和状态
     */
    private void initializeTestData() {
        log.info("开始初始化S7内存测试数据...");

        // 初始化DB块数据
        initializeDBMemory();

        // 初始化输入区域数据
        initializeInputMemory();

        // 初始化输出区域数据
        initializeOutputMemory();

        // 初始化位存储器数据
        initializeMarkerMemory();

        log.info("S7内存测试数据初始化完成");
    }

    /**
     * 初始化DB块内存
     */
    private void initializeDBMemory() {
        dbLock.writeLock().lock();
        try {
            // DB1 - 模拟电机控制数据
            // DB1.DBX0.0: 电机1运行状态
            dbMemory[0] = (byte) 0x01;
            // DB1.DBX0.1: 电机1故障状态
            dbMemory[0] |= (1 << 1);
            // DB1.DBX0.2: 电机1就绪状态
            dbMemory[0] |= (1 << 2);

            // DB1.DBW2: 电机1转速 (RPM)
            dbMemory[2] = 0x03;
            dbMemory[3] = (byte) 0xE8; // 1000 RPM

            // DB1.DBD4: 电机1电流 (mA)
            dbMemory[4] = 0x00;
            dbMemory[5] = 0x00;
            dbMemory[6] = 0x07;
            dbMemory[7] = (byte) 0xD0; // 2000 mA

            // DB1.DBD8: 电机1温度 (摄氏度 * 10)
            dbMemory[8] = 0x00;
            dbMemory[9] = 0x00;
            dbMemory[10] = 0x00;
            dbMemory[11] = (byte) 0xC8; // 20.0°C

            // DB1.DBX12.0: 泵1运行状态
            dbMemory[12] = (byte) 0x01;
            // DB1.DBX12.1: 泵1故障状态
            dbMemory[12] |= (1 << 1);

            // DB1.DBD16: 泵1压力 (bar * 100)
            dbMemory[16] = 0x00;
            dbMemory[17] = 0x00;
            dbMemory[18] = 0x03;
            dbMemory[19] = (byte) 0xE8; // 10.00 bar

            // DB1.DBD20: 生产计数器
            dbMemory[20] = 0x00;
            dbMemory[21] = 0x01;
            dbMemory[22] = (byte) 0x86;
            dbMemory[23] = (byte) 0xA0; // 100000 件

            // DB1.DBD24: 系统运行时间 (秒)
            dbMemory[24] = 0x00;
            dbMemory[25] = 0x00;
            dbMemory[26] = 0x0E;
            dbMemory[27] = (byte) 0x10; // 3600 秒 = 1小时

            log.debug("DB块测试数据初始化完成");
        } finally {
            dbLock.writeLock().unlock();
        }
    }

    /**
     * 初始化输入区域内存
     */
    private void initializeInputMemory() {
        inputLock.writeLock().lock();
        try {
            // I0.0-I0.7: 数字输入信号
            inputMemory[0] = (byte) 0xAA; // 10101010 - 模拟传感器状态

            // I1.0-I1.7: 按钮和开关状态
            inputMemory[1] = (byte) 0x0F; // 00001111 - 启动按钮按下

            // I2.0-I2.7: 安全门状态
            inputMemory[2] = (byte) 0x01; // 00000001 - 安全门关闭

            // I3.0-I3.7: 急停按钮状态
            inputMemory[3] = (byte) 0x00; // 00000000 - 无急停

            // 模拟一些模拟量输入 (IW4, IW6)
            // IW4: 温度传感器1 (摄氏度 * 10)
            inputMemory[4] = 0x00;
            inputMemory[5] = (byte) 0xC8; // 20.0°C

            // IW6: 压力传感器1 (bar * 100)
            inputMemory[6] = 0x03;
            inputMemory[7] = (byte) 0xE8; // 10.00 bar

            log.debug("输入区域测试数据初始化完成");
        } finally {
            inputLock.writeLock().unlock();
        }
    }

    /**
     * 初始化输出区域内存
     */
    private void initializeOutputMemory() {
        outputLock.writeLock().lock();
        try {
            // Q0.0-Q0.7: 数字输出信号
            outputMemory[0] = (byte) 0x55; // 01010101 - 交替输出

            // Q1.0-Q1.7: 电机控制输出
            outputMemory[1] = (byte) 0x01; // 00000001 - 电机1启动

            // Q2.0-Q2.7: 阀门控制输出
            outputMemory[2] = (byte) 0x03; // 00000011 - 阀门1和2打开

            // 模拟一些模拟量输出 (QW4, QW6)
            // QW4: 变频器速度设定 (Hz * 10)
            outputMemory[4] = 0x00;
            outputMemory[5] = (byte) 0x32; // 5.0 Hz

            // QW6: 阀门开度设定 (% * 10)
            outputMemory[6] = 0x00;
            outputMemory[7] = (byte) 0x64; // 10.0%

            log.debug("输出区域测试数据初始化完成");
        } finally {
            outputLock.writeLock().unlock();
        }
    }

    /**
     * 初始化位存储器内存
     */
    private void initializeMarkerMemory() {
        markerLock.writeLock().lock();
        try {
            // M0.0-M0.7: 系统标志位
            markerMemory[0] = (byte) 0x01; // M0.0: 系统初始化完成标志

            // M1.0-M1.7: 报警标志位
            markerMemory[1] = (byte) 0x00; // 无报警

            // M2.0-M2.7: 控制标志位
            markerMemory[2] = (byte) 0x01; // M2.0: 自动模式

            // M3.0-M3.7: 通信状态标志
            markerMemory[3] = (byte) 0x01; // M3.0: 通信正常

            // 初始化一些随机数据用于测试
            for (int i = 4; i < 32; i++) {
                markerMemory[i] = (byte) random.nextInt(256);
            }

            log.debug("位存储器测试数据初始化完成");
        } finally {
            markerLock.writeLock().unlock();
        }
    }

    // 读取DB块数据
    public byte[] readDB(int dbNumber, int offset, int length) {
        dbLock.readLock().lock();
        try {
            // 边界检查
            if (offset < 0 || length <= 0) {
                log.warn("DB{}读取参数无效: offset={}, length={}", dbNumber, offset, length);
                return new byte[0];
            }

            int start = offset;
            int end = Math.min(start + length, dbMemory.length);
            int actualLength = end - start;

            if (actualLength <= 0) {
                log.warn("DB{}读取超出内存范围: offset={}, length={}", dbNumber, offset, length);
                return new byte[0];
            }

            byte[] result = Arrays.copyOfRange(dbMemory, start, end);
            log.debug("读取DB{}: offset={}, length={}, actualLength={}", dbNumber, offset, length, actualLength);
            return result;
        } finally {
            dbLock.readLock().unlock();
        }
    }

    // 写入DB块数据
    public void writeDB(int dbNumber, int offset, byte[] data) {
        if (data == null) {
            log.warn("写入DB{}数据为空", dbNumber);
            return;
        }

        dbLock.writeLock().lock();
        try {
            // 边界检查
            if (offset < 0 || data.length == 0) {
                log.warn("DB{}写入参数无效: offset={}, dataLength={}", dbNumber, offset, data.length);
                return;
            }

            int start = offset;
            int end = Math.min(start + data.length, dbMemory.length);
            int length = end - start;

            if (length > 0) {
                System.arraycopy(data, 0, dbMemory, start, length);
                log.debug("写入DB{}: offset={}, length={}", dbNumber, offset, length);
            } else {
                log.warn("DB{}写入超出内存范围: offset={}, dataLength={}", dbNumber, offset, data.length);
            }
        } finally {
            dbLock.writeLock().unlock();
        }
    }

    // 读取输入区域
    public byte[] readInput(int offset, int length) {
        inputLock.readLock().lock();
        try {
            // 边界检查
            if (offset < 0 || length <= 0) {
                log.warn("输入区域读取参数无效: offset={}, length={}", offset, length);
                return new byte[0];
            }

            int start = offset;
            int end = Math.min(start + length, inputMemory.length);
            int actualLength = end - start;

            if (actualLength <= 0) {
                log.warn("输入区域读取超出内存范围: offset={}, length={}", offset, length);
                return new byte[0];
            }

            byte[] result = Arrays.copyOfRange(inputMemory, start, end);
            log.debug("读取输入区域: offset={}, length={}, actualLength={}", offset, length, actualLength);
            return result;
        } finally {
            inputLock.readLock().unlock();
        }
    }

    // 写入输出区域
    public void writeOutput(int offset, byte[] data) {
        if (data == null) {
            log.warn("写入输出区域数据为空");
            return;
        }

        outputLock.writeLock().lock();
        try {
            // 边界检查
            if (offset < 0 || data.length == 0) {
                log.warn("输出区域写入参数无效: offset={}, dataLength={}", offset, data.length);
                return;
            }

            int start = offset;
            int end = Math.min(start + data.length, outputMemory.length);
            int length = end - start;

            if (length > 0) {
                System.arraycopy(data, 0, outputMemory, start, length);
                log.debug("写入输出区域: offset={}, length={}", offset, length);
            } else {
                log.warn("输出区域写入超出内存范围: offset={}, dataLength={}", offset, data.length);
            }
        } finally {
            outputLock.writeLock().unlock();
        }
    }

    // 读取位存储器
    public byte[] readMarker(int offset, int length) {
        markerLock.readLock().lock();
        try {
            // 边界检查
            if (offset < 0 || length <= 0) {
                log.warn("位存储器读取参数无效: offset={}, length={}", offset, length);
                return new byte[0];
            }

            int start = offset;
            int end = Math.min(start + length, markerMemory.length);
            int actualLength = end - start;

            if (actualLength <= 0) {
                log.warn("位存储器读取超出内存范围: offset={}, length={}", offset, length);
                return new byte[0];
            }

            byte[] result = Arrays.copyOfRange(markerMemory, start, end);
            log.debug("读取位存储器: offset={}, length={}, actualLength={}", offset, length, actualLength);
            return result;
        } finally {
            markerLock.readLock().unlock();
        }
    }

    // 写入位存储器
    public void writeMarker(int offset, byte[] data) {
        if (data == null) {
            log.warn("写入位存储器数据为空");
            return;
        }

        markerLock.writeLock().lock();
        try {
            // 边界检查
            if (offset < 0 || data.length == 0) {
                log.warn("位存储器写入参数无效: offset={}, dataLength={}", offset, data.length);
                return;
            }

            int start = offset;
            int end = Math.min(start + data.length, markerMemory.length);
            int length = end - start;

            if (length > 0) {
                System.arraycopy(data, 0, markerMemory, start, length);
                log.debug("写入位存储器: offset={}, length={}", offset, length);
            } else {
                log.warn("位存储器写入超出内存范围: offset={}, dataLength={}", offset, data.length);
            }
        } finally {
            markerLock.writeLock().unlock();
        }
    }

    // 获取指定地址的位值
    public boolean readBit(String address) {
        try {
            if (address == null || address.isEmpty()) {
                log.warn("读取位地址为空");
                return false;
            }

            String[] parts = address.split("\\.");
            if (parts.length < 3) {
                log.warn("位地址格式错误: {}", address);
                return false;
            }

            String area = parts[0];
            int byteOffset;
            int bitOffset;

            // 解析字节偏移量
            if (area.startsWith("DB")) {
                // DB地址格式: DB1.DBX0.0
                String bytePart = parts[1];
                if (bytePart.startsWith("DBX") || bytePart.startsWith("DBB") || bytePart.startsWith("DBW") || bytePart.startsWith("DBD")) {
                    byteOffset = Integer.parseInt(bytePart.substring(3));
                } else {
                    // 兼容 DB1.0.0 格式
                    byteOffset = Integer.parseInt(bytePart);
                }
            } else {
                // 其他区域格式: I0.0, Q0.0, M0.0
                String bytePart = parts[1];
                if (bytePart.startsWith("B") || bytePart.startsWith("W") || bytePart.startsWith("D")) {
                    byteOffset = Integer.parseInt(bytePart.substring(1));
                } else {
                    byteOffset = Integer.parseInt(bytePart);
                }
            }

            bitOffset = Integer.parseInt(parts[2]);

            byte[] data;
            if (area.startsWith("DB")) {
                int dbNumber = Integer.parseInt(area.substring(2));
                data = readDB(dbNumber, byteOffset, 1);
            } else if ("I".equals(area) || "IB".equals(area)) {
                data = readInput(byteOffset, 1);
            } else if ("Q".equals(area) || "QB".equals(area)) {
                data = readOutput(byteOffset, 1);
            } else if ("M".equals(area) || "MB".equals(area)) {
                data = readMarker(byteOffset, 1);
            } else {
                log.warn("不支持的地址区域: {}", area);
                return false;
            }

            if (data.length == 0) {
                log.warn("读取地址 {} 时数据为空", address);
                return false;
            }

            boolean result = (data[0] & (1 << bitOffset)) != 0;
            log.debug("读取位地址 {}: byteOffset={}, bitOffset={}, value={}", address, byteOffset, bitOffset, result);
            return result;
        } catch (NumberFormatException e) {
            log.error("位地址解析错误: {}", address, e);
            return false;
        } catch (Exception e) {
            log.error("读取位地址出错: {}", address, e);
            return false;
        }
    }

    // 设置指定地址的位值
    public void writeBit(String address, boolean value) {
        try {
            if (address == null || address.isEmpty()) {
                log.warn("写入位地址为空");
                return;
            }

            String[] parts = address.split("\\.");
            if (parts.length < 3) {
                log.warn("位地址格式错误: {}", address);
                return;
            }

            String area = parts[0];
            int byteOffset;
            int bitOffset;

            // 解析字节偏移量
            if (area.startsWith("DB")) {
                // DB地址格式: DB1.DBX0.0
                String bytePart = parts[1];
                if (bytePart.startsWith("DBX") || bytePart.startsWith("DBB") || bytePart.startsWith("DBW") || bytePart.startsWith("DBD")) {
                    byteOffset = Integer.parseInt(bytePart.substring(3));
                } else {
                    // 兼容 DB1.0.0 格式
                    byteOffset = Integer.parseInt(bytePart);
                }
            } else {
                // 其他区域格式: I0.0, Q0.0, M0.0
                String bytePart = parts[1];
                if (bytePart.startsWith("B") || bytePart.startsWith("W") || bytePart.startsWith("D")) {
                    byteOffset = Integer.parseInt(bytePart.substring(1));
                } else {
                    byteOffset = Integer.parseInt(bytePart);
                }
            }

            bitOffset = Integer.parseInt(parts[2]);

            byte[] data = new byte[1];
            if (area.startsWith("DB")) {
                int dbNumber = Integer.parseInt(area.substring(2));
                data = readDB(dbNumber, byteOffset, 1);
                if (data.length == 0) data = new byte[1];

                if (value) {
                    data[0] |= (1 << bitOffset);
                } else {
                    data[0] &= ~(1 << bitOffset);
                }

                writeDB(dbNumber, byteOffset, data);
            } else if ("Q".equals(area) || "QB".equals(area)) {
                data = readOutput(byteOffset, 1);
                if (data.length == 0) data = new byte[1];

                if (value) {
                    data[0] |= (1 << bitOffset);
                } else {
                    data[0] &= ~(1 << bitOffset);
                }

                writeOutput(byteOffset, data);
            } else if ("M".equals(area) || "MB".equals(area)) {
                data = readMarker(byteOffset, 1);
                if (data.length == 0) data = new byte[1];

                if (value) {
                    data[0] |= (1 << bitOffset);
                } else {
                    data[0] &= ~(1 << bitOffset);
                }

                writeMarker(byteOffset, data);
            } else {
                log.warn("不支持的地址区域: {}", area);
            }

            log.debug("写入位地址 {}: byteOffset={}, bitOffset={}, value={}", address, byteOffset, bitOffset, value);
        } catch (NumberFormatException e) {
            log.error("位地址解析错误: {}", address, e);
        } catch (Exception e) {
            log.error("写入位地址出错: {}", address, e);
        }
    }

    // 读取输出区域
    private byte[] readOutput(int offset, int length) {
        outputLock.readLock().lock();
        try {
            // 边界检查
            if (offset < 0 || length <= 0) {
                log.warn("输出区域读取参数无效: offset={}, length={}", offset, length);
                return new byte[0];
            }

            int start = offset;
            int end = Math.min(start + length, outputMemory.length);
            int actualLength = end - start;

            if (actualLength <= 0) {
                log.warn("输出区域读取超出内存范围: offset={}, length={}", offset, length);
                return new byte[0];
            }

            byte[] result = Arrays.copyOfRange(outputMemory, start, end);
            log.debug("读取输出区域: offset={}, length={}, actualLength={}", offset, length, actualLength);
            return result;
        } finally {
            outputLock.readLock().unlock();
        }
    }

    /**
     * 获取内存使用统计信息
     */
    public String getMemoryStats() {
        return String.format("DB: %d bytes, Input: %d bytes, Output: %d bytes, Marker: %d bytes",
                dbMemory.length, inputMemory.length, outputMemory.length, markerMemory.length);
    }

    /**
     * 重置所有内存区域为初始状态
     */
    public void resetMemory() {
        dbLock.writeLock().lock();
        inputLock.writeLock().lock();
        outputLock.writeLock().lock();
        markerLock.writeLock().lock();

        try {
            Arrays.fill(dbMemory, (byte) 0);
            Arrays.fill(inputMemory, (byte) 0);
            Arrays.fill(outputMemory, (byte) 0);
            Arrays.fill(markerMemory, (byte) 0);

            initializeTestData();
            log.info("S7内存已重置并重新初始化");
        } finally {
            markerLock.writeLock().unlock();
            outputLock.writeLock().unlock();
            inputLock.writeLock().unlock();
            dbLock.writeLock().unlock();
        }
    }

    /**
     * 读取字数据 (16位)
     */
    public byte[] readWord(String area, int offset) {
        switch (area) {
            case "DB":
                return readDB(1, offset, 2);
            case "I":
                return readInput(offset, 2);
            case "Q":
                return readOutput(offset, 2);
            case "M":
                return readMarker(offset, 2);
            default:
                log.warn("不支持的区域: {}", area);
                return new byte[0];
        }
    }

    /**
     * 读取双字数据 (32位)
     */
    public byte[] readDWord(String area, int offset) {
        switch (area) {
            case "DB":
                return readDB(1, offset, 4);
            case "I":
                return readInput(offset, 4);
            case "Q":
                return readOutput(offset, 4);
            case "M":
                return readMarker(offset, 4);
            default:
                log.warn("不支持的区域: {}", area);
                return new byte[0];
        }
    }
}

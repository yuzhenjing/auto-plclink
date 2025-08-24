// S7Memory.java
package com.yuzj.autolink.s7;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
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
    
    public S7Memory() {
        // 初始化一些默认值用于测试
        initializeTestData();
    }
    
    private void initializeTestData() {
        // DB1 初始化一些测试数据
        dbLock.writeLock().lock();
        try {
            // DB1.DBX0.0 - DB1.DBX0.7 设置为 0xAA (10101010)
            dbMemory[0] = (byte) 0xAA;
            // DB1.DBW2 设置为 0x1234
            dbMemory[2] = 0x12;
            dbMemory[3] = 0x34;
            // DB1.DBD4 设置为 0x12345678
            dbMemory[4] = 0x12;
            dbMemory[5] = 0x34;
            dbMemory[6] = 0x56;
            dbMemory[7] = 0x78;
        } finally {
            dbLock.writeLock().unlock();
        }
        
        log.info("S7内存初始化完成");
    }
    
    // 读取DB块数据
    public byte[] readDB(int dbNumber, int offset, int length) {
        dbLock.readLock().lock();
        try {
            int start = offset;
            int end = Math.min(start + length, dbMemory.length);
            int actualLength = end - start;
            
            if (actualLength <= 0) {
                return new byte[0];
            }
            
            return Arrays.copyOfRange(dbMemory, start, end);
        } finally {
            dbLock.readLock().unlock();
        }
    }
    
    // 写入DB块数据
    public void writeDB(int dbNumber, int offset, byte[] data) {
        dbLock.writeLock().lock();
        try {
            int start = offset;
            int end = Math.min(start + data.length, dbMemory.length);
            int length = end - start;
            
            if (length > 0) {
                System.arraycopy(data, 0, dbMemory, start, length);
                log.debug("写入DB{}: offset={}, length={}", dbNumber, offset, length);
            }
        } finally {
            dbLock.writeLock().unlock();
        }
    }
    
    // 读取输入区域
    public byte[] readInput(int offset, int length) {
        inputLock.readLock().lock();
        try {
            int start = offset;
            int end = Math.min(start + length, inputMemory.length);
            int actualLength = end - start;
            
            if (actualLength <= 0) {
                return new byte[0];
            }
            
            return Arrays.copyOfRange(inputMemory, start, end);
        } finally {
            inputLock.readLock().unlock();
        }
    }
    
    // 写入输出区域
    public void writeOutput(int offset, byte[] data) {
        outputLock.writeLock().lock();
        try {
            int start = offset;
            int end = Math.min(start + data.length, outputMemory.length);
            int length = end - start;
            
            if (length > 0) {
                System.arraycopy(data, 0, outputMemory, start, length);
                log.debug("写入输出区域: offset={}, length={}", offset, length);
            }
        } finally {
            outputLock.writeLock().unlock();
        }
    }
    
    // 读取位存储器
    public byte[] readMarker(int offset, int length) {
        markerLock.readLock().lock();
        try {
            int start = offset;
            int end = Math.min(start + length, markerMemory.length);
            int actualLength = end - start;
            
            if (actualLength <= 0) {
                return new byte[0];
            }
            
            return Arrays.copyOfRange(markerMemory, start, end);
        } finally {
            markerLock.readLock().unlock();
        }
    }
    
    // 写入位存储器
    public void writeMarker(int offset, byte[] data) {
        markerLock.writeLock().lock();
        try {
            int start = offset;
            int end = Math.min(start + data.length, markerMemory.length);
            int length = end - start;
            
            if (length > 0) {
                System.arraycopy(data, 0, markerMemory, start, length);
                log.debug("写入位存储器: offset={}, length={}", offset, length);
            }
        } finally {
            markerLock.writeLock().unlock();
        }
    }
    
    // 获取指定地址的位值
    public boolean readBit(String address) {
        try {
            String[] parts = address.split("\\.");
            if (parts.length < 3) return false;
            
            String area = parts[0];
            int byteOffset = Integer.parseInt(parts[1].substring(2)); // 去掉"DB"前缀
            int bitOffset = Integer.parseInt(parts[2]);
            
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
                return false;
            }
            
            if (data.length == 0) return false;
            
            return (data[0] & (1 << bitOffset)) != 0;
        } catch (Exception e) {
            log.error("读取位地址出错: {}", address, e);
            return false;
        }
    }
    
    // 设置指定地址的位值
    public void writeBit(String address, boolean value) {
        try {
            String[] parts = address.split("\\.");
            if (parts.length < 3) return;
            
            String area = parts[0];
            int byteOffset = Integer.parseInt(parts[1].substring(2));
            int bitOffset = Integer.parseInt(parts[2]);
            
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
            }
            // 可以添加其他区域的处理
        } catch (Exception e) {
            log.error("写入位地址出错: {}", address, e);
        }
    }
    
    // 读取输出区域
    private byte[] readOutput(int offset, int length) {
        outputLock.readLock().lock();
        try {
            int start = offset;
            int end = Math.min(start + length, outputMemory.length);
            int actualLength = end - start;
            
            if (actualLength <= 0) {
                return new byte[0];
            }
            
            return Arrays.copyOfRange(outputMemory, start, end);
        } finally {
            outputLock.readLock().unlock();
        }
    }
}

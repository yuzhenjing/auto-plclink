// S7SimulatorController.java
package com.yuzj.autolink.s7;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/s7simulator")
public class S7SimulatorController {

    @Resource
    private S7Simulator s7Simulator;

    @PostMapping("/start")
    public ApiResponse<String> startSimulator(@RequestParam(defaultValue = "102") int port) {
        try {
            s7Simulator.start(port);
            return ApiResponse.success("S7模拟器启动成功，端口: " + port);
        } catch (IOException e) {
            log.error("启动S7模拟器失败", e);
            return ApiResponse.error("启动失败: " + e.getMessage());
        }
    }

    @PostMapping("/stop")
    public ApiResponse<String> stopSimulator() {
        s7Simulator.stop();
        return ApiResponse.success("S7模拟器已停止");
    }

    @GetMapping("/status")
    public ApiResponse<String> getStatus() {
        if (s7Simulator.isRunning()) {
            return ApiResponse.success("运行中");
        } else {
            return ApiResponse.success("已停止");
        }
    }

    @PostMapping("/memory/write")
    public ApiResponse<String> writeMemory(@RequestParam String address,
                                           @RequestParam String value) {
        try {
            if (address.contains(".")) {
                // 位地址
                boolean bitValue = Boolean.parseBoolean(value);
                s7Simulator.getMemory().writeBit(address, bitValue);
            } else {
                // 字节地址
                // 这里可以添加字节写入逻辑
            }
            return ApiResponse.success("写入成功");
        } catch (Exception e) {
            log.error("写入内存失败", e);
            return ApiResponse.error("写入失败: " + e.getMessage());
        }
    }

    @GetMapping("/memory/read")
    public ApiResponse<String> readMemory(@RequestParam String address) {
        try {
            if (address.contains(".")) {
                // 位地址
                boolean value = s7Simulator.getMemory().readBit(address);
                return ApiResponse.success(String.valueOf(value));
            } else {
                // 字节地址
                return ApiResponse.success("0"); // 简化实现
            }
        } catch (Exception e) {
            log.error("读取内存失败", e);
            return ApiResponse.error("读取失败: " + e.getMessage());
        }
    }
}

// 统一响应类
class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }

    // getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

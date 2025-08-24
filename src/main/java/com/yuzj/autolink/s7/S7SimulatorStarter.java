package com.yuzj.autolink.s7;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

// 在主应用类中添加自动启动模拟器
@Component
@Slf4j
public class S7SimulatorStarter {

    @Resource
    private S7Simulator s7Simulator;

    @EventListener(ApplicationReadyEvent.class)
    public void startSimulator() {
        try {
            // 应用启动时自动启动S7模拟器
            s7Simulator.start(102);
            log.info("S7模拟器已自动启动");
        } catch (Exception e) {
            log.error("启动S7模拟器失败", e);
        }
    }
}

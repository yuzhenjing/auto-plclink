package com.yuzj.autolink.plc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class PackageHandlerService implements DataMonitorService.PackageListener {

    @Autowired
    private DataMonitorService dataMonitorService;

    @PostConstruct
    public void init() {
        // 注册打包监听器
        dataMonitorService.addPackageListener(this);
    }

    @Override
    public void onPackageCreated(DataMonitorService.DataPackage dataPackage) {
        // 处理打包数据
        System.out.println("收到数据包: " + dataPackage);
        System.out.println("数据包大小: " + dataPackage.getSize());

        // 在这里实现您的打包业务逻辑
        // 例如：保存到数据库、发送到其他系统等
        processPackageData(dataPackage);
    }

    private void processPackageData(DataMonitorService.DataPackage dataPackage) {
        // 实现您的业务逻辑
        // 例如保存到数据库
        // packageRepository.save(convertToEntity(dataPackage));
    }
}

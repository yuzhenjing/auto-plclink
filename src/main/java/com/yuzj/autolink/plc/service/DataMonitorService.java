package com.yuzj.autolink.plc.service;

import com.yuzj.autolink.domain.PlcDataRecord;
import com.yuzj.autolink.domain.PlcTagModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class DataMonitorService {

    // 数据包大小配置
    private static final int PACKAGE_SIZE = 10;

    // 数据缓存队列
    private final Queue<PlcDataRecord> dataQueue = new ConcurrentLinkedQueue<>();

    // 打包超时时间（毫秒），例如30秒
    private volatile long packageTimeoutMs = 30000;

    // 上次打包时间
    private volatile LocalDateTime lastPackageTime = LocalDateTime.now();

    private final List<PlcTagModel> monitoredTags = new CopyOnWriteArrayList<>();
    private final AtomicBoolean monitoring = new AtomicBoolean(false);
    private final List<DataChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final List<PackageListener> packageListeners = new CopyOnWriteArrayList<>();
    private volatile int refreshInterval = 100;

    @Resource
    private List<PlcService> services;

    public void startMonitoring(List<PlcTagModel> tags) {
        if (tags == null) {
            log.warn("传入的产品列表为null，无法启动监控");
            return;
        }

        this.monitoredTags.clear();
        this.monitoredTags.addAll(tags);
        this.monitoring.set(true);
        log.info("开始监控 {} 个产品", tags.size());
    }

    public void stopMonitoring() {
        if (this.monitoring.get()) {
            this.monitoring.set(false);
            // 停止监控时处理剩余数据
            processRemainingData();
            log.info("已停止产品监控");
        }
    }

    public boolean isMonitoring() {
        return monitoring.get();
    }

    public void setRefreshInterval(int intervalMs) {
        if (intervalMs <= 0) {
            throw new IllegalArgumentException("刷新间隔必须大于0");
        }
        this.refreshInterval = intervalMs;
    }

    // 设置打包超时时间
    public void setPackageTimeout(long timeoutMs) {
        this.packageTimeoutMs = timeoutMs;
    }

    @Async
    @Scheduled(fixedRateString = "${plc.monitor.interval-ms:100}")
    public void monitor() {
        // 检查是否应该进行监控
        for (PlcService service : services) {
            if (!monitoring.get() || !service.isConnected()) {
                continue;
            }

            List<PlcDataRecord> records = new ArrayList<>();
            for (PlcTagModel tag : monitoredTags) {
                // 跳过禁用的产品
                if (!tag.isEnabled()) {
                    continue;
                }

                try {
                    Object value = service.read(tag.getAddress());
                    PlcDataRecord record = new PlcDataRecord(
                            tag.getName(),
                            value,
                            tag.getDataType()
                    );
                    records.add(record);

                    // 通知监听器
                    notifyDataChange(record);

                    // 将数据添加到队列
                    dataQueue.offer(record);

                } catch (Exception e) {
                    log.error("监控产品 {} 时出错", tag.getName(), e);

                    // 创建错误记录
                    PlcDataRecord errorRecord = new PlcDataRecord(
                            tag.getName(),
                            "ERROR",
                            tag.getDataType()
                    );
                    errorRecord.qualityProperty().set("Bad");
                    notifyDataChange(errorRecord);
                }
            }

            // 处理数据打包
            processDataPackaging();

            // 保存到数据库或进行其他处理
            if (!records.isEmpty()) {
                log.debug("本次监控收集到 {} 条记录", records.size());
            }
        }
    }

    /**
     * 处理数据打包逻辑
     */
    private void processDataPackaging() {
        boolean shouldPackage = false;
        List<PlcDataRecord> packageData = new ArrayList<>();

        // 检查是否达到打包数量
        if (dataQueue.size() >= PACKAGE_SIZE) {
            shouldPackage = true;
            // 取出10个数据
            for (int i = 0; i < PACKAGE_SIZE; i++) {
                packageData.add(dataQueue.poll());
            }
        }
        // 检查是否超时且队列不为空
        else if (!dataQueue.isEmpty() &&
                LocalDateTime.now().isAfter(lastPackageTime.plusNanos(packageTimeoutMs * 1000000))) {
            shouldPackage = true;
            // 取出所有剩余数据
            while (!dataQueue.isEmpty()) {
                packageData.add(dataQueue.poll());
            }
        }

        // 执行打包
        if (shouldPackage && !packageData.isEmpty()) {
            DataPackage dataPackage = new DataPackage(packageData, LocalDateTime.now());
            notifyPackageCreated(dataPackage);
            lastPackageTime = LocalDateTime.now();
            log.info("创建数据包，包含 {} 条记录", packageData.size());
        }
    }

    /**
     * 处理剩余数据（在停止监控时调用）
     */
    private void processRemainingData() {
        if (!dataQueue.isEmpty()) {
            List<PlcDataRecord> remainingData = new ArrayList<>();
            while (!dataQueue.isEmpty()) {
                remainingData.add(dataQueue.poll());
            }
            DataPackage dataPackage = new DataPackage(remainingData, LocalDateTime.now());
            notifyPackageCreated(dataPackage);
            log.info("处理剩余数据，创建最终数据包，包含 {} 条记录", remainingData.size());
        }
    }

    public void addDataChangeListener(DataChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeDataChangeListener(DataChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    // 添加打包监听器
    public void addPackageListener(PackageListener listener) {
        if (listener != null) {
            packageListeners.add(listener);
        }
    }

    public void removePackageListener(PackageListener listener) {
        if (listener != null) {
            packageListeners.remove(listener);
        }
    }

    private void notifyDataChange(PlcDataRecord record) {
        // 使用普通for循环避免迭代器开销
        for (int i = 0; i < listeners.size(); i++) {
            try {
                DataChangeListener listener = listeners.get(i);
                listener.onDataChanged(record);
            } catch (Exception e) {
                log.error("通知监听器时发生错误", e);
            }
        }
    }

    private void notifyPackageCreated(DataPackage dataPackage) {
        for (int i = 0; i < packageListeners.size(); i++) {
            try {
                PackageListener listener = packageListeners.get(i);
                listener.onPackageCreated(dataPackage);
            } catch (Exception e) {
                log.error("通知打包监听器时发生错误", e);
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        stopMonitoring();
        listeners.clear();
        packageListeners.clear();
        log.info("DataMonitorService 已清理完成");
    }

    public interface DataChangeListener {
        void onDataChanged(PlcDataRecord record);
    }

    /**
     * 数据包监听器接口
     */
    public interface PackageListener {
        void onPackageCreated(DataPackage dataPackage);
    }

    /**
     * 数据包实体类
     */
    public static class DataPackage {
        private final List<PlcDataRecord> records;
        private final LocalDateTime createTime;
        private final String packageId;

        public DataPackage(List<PlcDataRecord> records, LocalDateTime createTime) {
            this.records = new ArrayList<>(records);
            this.createTime = createTime;
            this.packageId = UUID.randomUUID().toString();
        }

        // Getters
        public List<PlcDataRecord> getRecords() {
            return new ArrayList<>(records);
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getPackageId() {
            return packageId;
        }

        public int getSize() {
            return records.size();
        }

        @Override
        public String toString() {
            return "DataPackage{" +
                    "size=" + records.size() +
                    ", createTime=" + createTime +
                    ", packageId='" + packageId + '\'' +
                    '}';
        }
    }
}

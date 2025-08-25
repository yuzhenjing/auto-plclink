package com.yuzj.autolink.plc.control;

import com.yuzj.autolink.domain.AlarmRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yuzj002
 */
@Slf4j
@Component
public class AlarmControl extends BaseControl {
    // 报警管理组件
    @FXML
    protected TableView<AlarmRecord> alarmTable;
    // 报警阈值配置
    protected final Map<String, Double> alarmThresholds = new HashMap<>();

    private final ObservableList<AlarmRecord> alarmRecords = FXCollections.observableArrayList();

    protected void initializeAlarmThresholds() {
        alarmThresholds.put("温度传感器", 50.0);
        alarmThresholds.put("压力传感器", 100.0);
        log.info("报警阈值初始化完成");
    }

    protected void initializeAlarmTable() {
        alarmTable.setItems(alarmRecords);
        log.info("报警表格初始化完成");
    }

    @FXML
    protected void handleAcknowledgeAlarms() {
        int count = 0;
        for (AlarmRecord alarm : alarmRecords) {
            if (!alarm.isAcknowledged()) {
                alarm.acknowledge();
                count++;
            }
        }
        alarmTable.refresh();
        logMessage("已确认 " + count + " 个报警");
        updateStatus("已确认报警");
        log.info("已确认 {} 个报警", count);
    }

    @FXML
    protected void handleClearAlarms() {
        int count = alarmRecords.size();
        alarmRecords.clear();
        logMessage("已清除 " + count + " 条报警记录");
        updateStatus("已清除报警记录");
        log.info("已清除 {} 条报警记录", count);
    }

}

package com.yuzj.autolink.plc.control;

import com.yuzj.autolink.dao.model.AlarmRecord;
import com.yuzj.autolink.dao.repository.AlarmRecordRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报警管理控制器
 *
 * @author yuzj002
 */
@Slf4j
@Component
public class AlarmControl extends BaseControl {

    // 报警管理组件
    @FXML
    protected TableView<AlarmRecord> alarmTable;

    @FXML
    private TableColumn<AlarmRecord, String> tagNameColumn;

    @FXML
    private TableColumn<AlarmRecord, String> alarmTypeColumn;

    @FXML
    private TableColumn<AlarmRecord, String> alarmMessageColumn;

    @FXML
    private TableColumn<AlarmRecord, String> alarmLevelColumn;

    @FXML
    private TableColumn<AlarmRecord, String> alarmValueColumn;

    @FXML
    private TableColumn<AlarmRecord, String> timestampColumn;

    @FXML
    private TableColumn<AlarmRecord, String> acknowledgedColumn;

    @FXML
    private TableColumn<AlarmRecord, String> acknowledgedTimeColumn;

    @FXML
    private Label totalAlarmCount;
    @FXML
    private Label criticalAlarmCount;
    @FXML
    private Label unacknowledgedCount;


    @Resource
    private AlarmRecordRepository alarmRecordRepository;

    // 报警数据列表
    private final ObservableList<AlarmRecord> alarmData = FXCollections.observableArrayList();

    // 时间格式化器
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {

        // 初始化表格列
        setupTableColumns();

        // 绑定数据到表格
        alarmTable.setItems(alarmData);

        // 初始化状态
        updateStatus("报警管理模块初始化完成");

        // 加载初始数据
        loadAlarmData();

        log.info("报警管理控制器初始化完成");
    }

    /**
     * 设置表格列
     */
    private void setupTableColumns() {
        tagNameColumn.setCellValueFactory(cellData -> createStringProperty(cellData.getValue().getTagName()));
        alarmTypeColumn.setCellValueFactory(cellData -> createStringProperty(cellData.getValue().getAlarmType()));
        alarmMessageColumn.setCellValueFactory(cellData -> createStringProperty(cellData.getValue().getAlarmMessage()));
        alarmLevelColumn.setCellValueFactory(cellData -> createStringProperty(cellData.getValue().getAlarmLevel()));
        alarmValueColumn.setCellValueFactory(cellData -> createStringProperty(cellData.getValue().getAlarmValue()));
        timestampColumn.setCellValueFactory(cellData -> createFormattedTimeProperty(cellData.getValue().getTimestamp()));
        acknowledgedColumn.setCellValueFactory(cellData -> createAcknowledgedStatusProperty(cellData.getValue().getIsAcknowledged()));
        acknowledgedTimeColumn.setCellValueFactory(cellData -> createFormattedTimeProperty(cellData.getValue().getAcknowledgedTime()));
    }

    /**
     * 创建字符串属性的辅助方法
     */
    private javafx.beans.property.SimpleStringProperty createStringProperty(String value) {
        return new javafx.beans.property.SimpleStringProperty(value != null ? value : "");
    }

    /**
     * 创建格式化时间属性的辅助方法
     */
    private javafx.beans.property.SimpleStringProperty createFormattedTimeProperty(LocalDateTime time) {
        return new javafx.beans.property.SimpleStringProperty(
                time != null ? time.format(TIME_FORMATTER) : "");
    }

    /**
     * 创建确认状态属性的辅助方法
     */
    private javafx.beans.property.SimpleStringProperty createAcknowledgedStatusProperty(Boolean isAcknowledged) {
        String status = (isAcknowledged != null && isAcknowledged) ? "已确认" : "未确认";
        return new javafx.beans.property.SimpleStringProperty(status);
    }

    /**
     * 加载报警数据
     */
    private void loadAlarmData() {
        try {
            List<AlarmRecord> alarms = alarmRecordRepository.list();
            alarmData.clear();
            alarmData.addAll(alarms);
            totalAlarmCount.setText(String.valueOf(alarms.size()));
            criticalAlarmCount.setText(String.valueOf(alarmRecordRepository.lambdaQuery().eq(AlarmRecord::getAlarmLevel, "CRITICAL").count()));
            unacknowledgedCount.setText(String.valueOf(alarmRecordRepository.lambdaQuery().eq(AlarmRecord::getIsAcknowledged, false).count()));
            updateStatus("已加载 " + alarms.size() + " 条报警记录");
            log.info("成功加载 {} 条报警记录", alarms.size());
        } catch (Exception e) {
            log.error("加载报警数据失败", e);
            updateStatus("加载报警数据失败: " + e.getMessage());
            showErrorAlert("数据加载失败", "无法从数据库加载报警数据: " + e.getMessage());
        }
    }

    /**
     * 刷新报警数据
     */
    private void refreshAlarmData() {
        loadAlarmData();
        alarmTable.refresh();
        log.debug("报警数据已刷新");
    }

    /**
     * 处理确认报警
     */
    @FXML
    protected void handleAcknowledgeAlarms() {
        try {
            // 获取选中的报警记录
            List<AlarmRecord> selectedAlarms = alarmTable.getSelectionModel().getSelectedItems();

            if (selectedAlarms.isEmpty()) {
                showWarningAlert("确认报警", "请先选择要确认的报警记录");
                return;
            }

            // 获取选中记录的ID
            List<Long> alarmIds = selectedAlarms.stream()
                    .map(AlarmRecord::getId)
                    .collect(Collectors.toList());

            boolean update = alarmRecordRepository.lambdaUpdate()
                    .set(AlarmRecord::getIsAcknowledged, true)
                    .set(AlarmRecord::getAcknowledgedTime, LocalDateTime.now())
                    .in(AlarmRecord::getId, alarmIds)
                    .update();
            // 确认报警
            if (update) {
                // 刷新数据
                refreshAlarmData();
                String message = "已确认 " + alarmIds.size() + " 个报警";
                logMessage(message);
                updateStatus(message);
                log.info("已确认 {} 个报警", alarmIds.size());
                showInfoAlert("确认报警", message);
            } else {
                showErrorAlert("确认报警", "确认报警操作失败");
            }
        } catch (Exception e) {
            log.error("确认报警失败", e);
            showErrorAlert("确认报警", "确认报警操作失败: " + e.getMessage());
        }
    }

    /**
     * 处理清除报警
     */
    @FXML
    protected void handleClearAlarms() {
        try {
            // 获取选中的报警记录
            List<AlarmRecord> selectedAlarms = alarmTable.getSelectionModel().getSelectedItems();

            if (selectedAlarms.isEmpty()) {
                showWarningAlert("清除报警", "请先选择要清除的报警记录");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("确认清除");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("确定要清除选中的 " + selectedAlarms.size() + " 条报警记录吗？此操作不可恢复。");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // 获取选中记录的ID
                        List<Long> alarmIds = selectedAlarms.stream()
                                .map(AlarmRecord::getId)
                                .collect(Collectors.toList());

                        // 从数据库中删除记录
                        boolean removed = alarmRecordRepository.removeByIds(alarmIds);

                        if (removed) {
                            // 刷新数据
                            refreshAlarmData();

                            String message = "已清除 " + alarmIds.size() + " 条报警记录";
                            logMessage(message);
                            updateStatus(message);
                            log.info(message);

                            showInfoAlert("清除报警", message);
                        } else {
                            showErrorAlert("清除报警", "清除报警操作失败");
                        }
                    } catch (Exception e) {
                        log.error("清除报警失败", e);
                        showErrorAlert("清除报警", "清除报警操作失败: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            log.error("清除报警失败", e);
            showErrorAlert("清除报警", "清除报警操作失败: " + e.getMessage());
        }
    }

    /**
     * 处理刷新数据
     */
    @FXML
    protected void handleRefresh() {
        refreshAlarmData();
        showInfoAlert("刷新数据", "报警数据已刷新");
    }

    /**
     * 处理确认所有未确认报警
     */
    @FXML
    protected void handleAcknowledgeAll() {
        try {
            List<AlarmRecord> unacknowledgedAlarms = alarmRecordRepository
                    .lambdaQuery().eq(AlarmRecord::getIsAcknowledged, false).list();

            if (unacknowledgedAlarms.isEmpty()) {
                showInfoAlert("确认所有报警", "当前没有未确认的报警");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("确认所有报警");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("确定要确认所有 " + unacknowledgedAlarms.size() + " 个未确认的报警吗？");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // 获取未确认报警的ID列表
                        List<Long> alarmIds = unacknowledgedAlarms.stream()
                                .map(AlarmRecord::getId)
                                .collect(Collectors.toList());

                        // 确认所有未确认的报警
                        boolean update = alarmRecordRepository.lambdaUpdate()
                                .set(AlarmRecord::getIsAcknowledged, true)
                                .set(AlarmRecord::getAcknowledgedTime, LocalDateTime.now())
                                .in(AlarmRecord::getId, alarmIds)
                                .update();

                        // 确认报警
                        if (update) {
                            // 刷新数据
                            refreshAlarmData();

                            String message = "已确认所有 " + alarmIds.size() + " 个报警";
                            logMessage(message);
                            updateStatus(message);
                            log.info(message);

                            showInfoAlert("确认所有报警", message);
                        } else {
                            showErrorAlert("确认所有报警", "确认操作失败");
                        }
                    } catch (Exception e) {
                        log.error("确认所有报警失败", e);
                        showErrorAlert("确认所有报警", "确认操作失败: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            log.error("确认所有报警失败", e);
            showErrorAlert("确认所有报警", "操作失败: " + e.getMessage());
        }
    }
}

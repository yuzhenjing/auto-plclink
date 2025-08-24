package com.yuzj.autolink.plc.controller;

import com.yuzj.autolink.domain.PlcTagModel;
import com.yuzj.autolink.plc.listener.DataMonitorService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yuzj002
 */
@Slf4j
public class BaseController {

    // 日志组件
    @FXML
    protected TextArea logArea;
    // 状态栏组件
    @FXML
    protected Label statusLabel;

    @FXML
    protected ComboBox<String> protocolCombo;

    // 图表组件
    @FXML
    protected LineChart<Number, Number> dataChart;

    @Resource
    public DataMonitorService dataMonitorService;

    public final Map<String, XYChart.Series<Number, Number>> chartSeries = new ConcurrentHashMap<>();

    public final ObservableList<PlcTagModel> tagConfigs = FXCollections.observableArrayList();

    public void startMonitoringConfiguredTags() {
        List<PlcTagModel> enabledTags = new ArrayList<>();
        for (PlcTagModel tag : tagConfigs) {
            if (tag.isEnabled()) {
                enabledTags.add(tag);
            }
        }
        dataMonitorService.startMonitoring(enabledTags);
        // 初始化图表系列
        dataChart.getData().clear();

        chartSeries.clear();

        for (PlcTagModel tag : enabledTags) {
            String dataType = tag.getDataType();
            // 布尔值和字符串不适合图表显示
            if (!"BOOL".equals(dataType) && !"STRING".equals(dataType)) {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(tag.getName());
                dataChart.getData().add(series);
                chartSeries.put(tag.getName(), series);
            }
        }

        logMessage("开始监控 " + enabledTags.size() + " 个产品");
        updateStatus("开始监控 " + enabledTags.size() + " 个产品");
        log.info("开始监控 {} 个产品", enabledTags.size());
    }

    public void showInfoAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    public void showWarningAlert(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    public void showErrorAlert(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    public void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void updateStatus(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
            log.debug("更新状态栏: {}", text);
        } else {
            log.debug("状态栏未初始化，无法更新状态: {}", text);
        }
    }

    public void logMessage(String message) {
        if (logArea != null) {
            Platform.runLater(() -> {
                String timestamp = LocalTime.now().toString();
                logArea.appendText("[" + timestamp + "] " + message + "\n");
                // 自动滚动到底部
                logArea.setScrollTop(Double.MAX_VALUE);
            });
        } else {
            log.debug("日志区域未初始化，无法记录消息: {}", message);
        }
    }

    public void updateConnectionButtons(Button button, boolean connected) {
        button.setDisable(connected);
        log.debug("更新连接按钮状态: connected={}", connected);
    }

}

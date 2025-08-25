package com.yuzj.autolink.plc.control;

import com.yuzj.autolink.domain.PlcTagModel;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yuzj002
 */
@Slf4j
@Component
public class ChartControl extends BaseControl {

    @FXML
    private ComboBox<String> chartTagSelector;
    // 图表组件
    @FXML
    protected LineChart<Number, Number> dataChart;
    // 图表系列
    public final Map<String, XYChart.Series<Number, Number>> chartSeries = new ConcurrentHashMap<>();

    @FXML
    public void initialize() {
        tagConfigs.addAll(
                new PlcTagModel("温度传感器", "DB10.DBW0", "INT", "车间温度传感器"),
                new PlcTagModel("压力传感器", "DB10.DBW2", "INT", "管道压力传感器"),
                new PlcTagModel("电机状态", "DB10.DBX4.0", "BOOL", "主电机运行状态"),
                new PlcTagModel("流量计", "DB10.DBD6", "REAL", "液体流量计"),
                new PlcTagModel("设备开关", "DB10.DBX10.0", "BOOL", "设备总开关"),
                new PlcTagModel("运行速度", "DB10.DBD12", "REAL", "conveyor运行速度")
        );
    }

    @FXML
    private void handleAddChart() {
        String selectedTag = chartTagSelector.getValue();
        if (selectedTag == null || selectedTag.isEmpty()) {
            showWarningAlert("选择错误", "请先选择一个监控点");
            updateStatus("请选择监控点");
            return;
        }

        if (!chartSeries.containsKey(selectedTag)) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(selectedTag);
            dataChart.getData().add(series);
            chartSeries.put(selectedTag, series);
            logMessage("已添加图表系列: " + selectedTag);
            updateStatus("已添加图表系列");
            log.info("已添加图表系列: {}", selectedTag);
        } else {
            showInfoAlert("提示", "该监控点已在图表中显示");
            updateStatus("监控点已在图表中");
        }
    }

    @FXML
    private void handleClearChart() {
        dataChart.getData().clear();
        chartSeries.clear();
        logMessage("已清除图表数据");
        updateStatus("已清除图表数据");
        log.info("已清除图表数据");
    }
}

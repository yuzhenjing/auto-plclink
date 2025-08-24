package com.yuzj.autolink.plc.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

/**
 * @author yuzj002
 */
@Slf4j
@Controller
public class ChartController extends BaseController {

    @FXML
    private ComboBox<String> chartTagSelector;

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

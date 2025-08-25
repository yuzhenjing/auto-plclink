package com.yuzj.autolink.plc.control;

import com.yuzj.autolink.domain.PlcTagModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author yuzj002
 */
@Slf4j
@Component
public class MonitoringControl extends BaseControl {

    @FXML
    private TextField refreshIntervalField;


    @FXML
    private void handleAddTag() {
        Dialog<PlcTagModel> dialog = new Dialog<>();
        dialog.setTitle("添加监控点");
        dialog.setHeaderText("请输入产品信息");

        // 设置按钮
        ButtonType addButtonType = new ButtonType("添加", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // 创建输入字段
        TextField nameField = new TextField();
        nameField.setPromptText("产品名称");
        TextField addressField = new TextField();
        addressField.setPromptText("地址");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList("BOOL", "BYTE", "INT", "DINT", "REAL", "STRING"));
        typeCombo.getSelectionModel().selectFirst();
        TextField descField = new TextField();
        descField.setPromptText("描述");

        // 创建布局
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label("产品名称:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("地址:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("数据类型:"), 0, 2);
        grid.add(typeCombo, 1, 2);
        grid.add(new Label("描述:"), 0, 3);
        grid.add(descField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // 请求焦点到第一个输入框
        Platform.runLater(() -> nameField.requestFocus());

        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new PlcTagModel(
                        nameField.getText(),
                        addressField.getText(),
                        typeCombo.getValue(),
                        descField.getText()
                );
            }
            return null;
        });

        Optional<PlcTagModel> result = dialog.showAndWait();
        result.ifPresent(tagConfig -> {
            tagConfigs.add(tagConfig);
            updateChartTagSelector();
            logMessage("添加了新监控点: " + tagConfig.getName());
            updateStatus("已添加监控点");
            log.info("添加了新监控点: {}", tagConfig.getName());
        });
    }

    @FXML
    private void handleClearData() {
//        int count = dataRecords.size();
//        dataRecords.clear();
//        dataPointCount = 0;
//        dataPointCountLabel.setText("0");
//        logMessage("已清除 " + count + " 条数据记录");
//        updateStatus("已清除数据记录");
//        log.info("清除了 {} 条数据记录", count);
    }

    private void updateChartTagSelector() {
        ObservableList<String> tagNames = FXCollections.observableArrayList();
        for (PlcTagModel tag : tagConfigs) {
            // 布尔值和字符串不适合图表显示
            String dataType = tag.getDataType();
            if (!"BOOL".equals(dataType) && !"STRING".equals(dataType)) {
                tagNames.add(tag.getName());
            }
        }
//        chartTagSelector.setItems(tagNames);
        log.debug("更新图表产品选择器，共有 {} 个可选项", tagNames.size());
    }

    public void addExampleTagConfigs() {
        tagConfigs.addAll(
                new PlcTagModel("温度传感器", "DB10.DBW0", "INT", "车间温度传感器"),
                new PlcTagModel("压力传感器", "DB10.DBW2", "INT", "管道压力传感器"),
                new PlcTagModel("电机状态", "DB10.DBX4.0", "BOOL", "主电机运行状态"),
                new PlcTagModel("流量计", "DB10.DBD6", "REAL", "液体流量计"),
                new PlcTagModel("设备开关", "DB10.DBX10.0", "BOOL", "设备总开关"),
                new PlcTagModel("运行速度", "DB10.DBD12", "REAL", "conveyor运行速度")
        );

        updateChartTagSelector();
        log.info("添加了 {} 个示例产品配置", tagConfigs.size());
    }

    @FXML
    private void handleApplyRefreshInterval() {
        try {
            int interval = Integer.parseInt(refreshIntervalField.getText());
            if (interval < 10) {
                showWarningAlert("输入错误", "刷新间隔不能小于10ms");
                updateStatus("刷新间隔不能小于10ms");
                return;
            }
            logMessage("刷新间隔已设置为: " + interval + "ms");
            updateStatus("刷新间隔已更新");
            log.info("刷新间隔已设置为: {}ms", interval);

        } catch (NumberFormatException e) {
            showErrorAlert("输入错误", "请输入有效的数字");
            updateStatus("请输入有效数字");
            log.warn("刷新间隔输入格式错误: {}", refreshIntervalField.getText());
        }
    }
}

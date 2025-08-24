// 文件路径: src/main/java/com/yuzj/autolink/plc/controller/MainController.java
package com.yuzj.autolink.plc.controller;

import com.yuzj.autolink.domain.PlcDataRecord;
import com.yuzj.autolink.domain.PlcTagModel;
import com.yuzj.autolink.plc.handler.SecurityHandler;
import com.yuzj.autolink.plc.service.DataMonitorService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.util.Optional;

/**
 * @author yuzj002
 */
@Slf4j
@Controller
public class MainController extends AlarmController implements DataMonitorService.DataChangeListener {

    // 安全管理器实例
    private final SecurityHandler securityHandler = SecurityHandler.getInstance();

    //数据监控组件
    @FXML
    private TableView<PlcDataRecord> dataTable;
    @FXML
    private TextField refreshIntervalField;
    @FXML
    private ToggleButton autoRefreshToggle;

    @FXML
    private ComboBox<String> chartTagSelector;

    // 读写操作组件
    @FXML
    private TextField readWriteAddressField;
    @FXML
    private TextField readWriteValueField;
    @FXML
    private ComboBox<String> dataTypeCombo;

    // 系统配置组件
    @FXML
    private TableView<PlcTagModel> configTable;

    @FXML
    private Label dataPointCountLabel;
    @FXML
    private Label latencyLabel;

    private final ObservableList<PlcDataRecord> dataRecords = FXCollections.observableArrayList();
    private volatile int dataPointCount = 0;

    @FXML
    public void initialize() {
        log.info("初始化主控制器");

        // 初始化协议选择
        protocolCombo.setItems(FXCollections.observableArrayList(
                "S7", "MODBUS_TCP", "MODBUS_RTU", "OPC_UA"
        ));
        protocolCombo.getSelectionModel().selectFirst();

        // 初始化数据类型选择
        dataTypeCombo.setItems(FXCollections.observableArrayList(
                "BOOL", "BYTE", "INT", "DINT", "REAL", "STRING"
        ));
        dataTypeCombo.getSelectionModel().selectFirst();

        // 初始化表格
        initializeDataTable();
        initializeAlarmTable();
        initializeConfigTable();

        // 注册数据监听器
        dataMonitorService.addDataChangeListener(this);

        // 设置连接按钮状态
        updateConnectionButtons(false);

        // 添加示例产品配置
        addExampleTagConfigs();

        // 初始化报警阈值
        initializeAlarmThresholds();

        // 初始化状态栏
        updateStatus("就绪");
        dataPointCountLabel.setText("0");
        latencyLabel.setText("0 ms");

        // 禁用窗口关闭功能
        disableWindowClose();

        log.info("主控制器初始化完成");
    }

    /**
     * 禁用窗口关闭功能
     */
    private void disableWindowClose() {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) dataTable.getScene().getWindow();
                stage.setOnCloseRequest(this::handleCloseRequest);
            } catch (Exception e) {
                log.error("设置窗口关闭处理器时出错", e);
            }
        });
    }

    /**
     * 处理窗口关闭请求
     *
     * @param event 窗口事件
     */
    private void handleCloseRequest(WindowEvent event) {
        // 阻止窗口关闭
        event.consume();
        log.info("用户尝试关闭窗口，已阻止该操作");

        // 显示账号密码输入对话框
        Platform.runLater(() -> showExitDialog());
    }

    /**
     * 显示退出确认对话框
     */
    private void showExitDialog() {
        Dialog<SecurityHandler.UserCredential> dialog = new Dialog<>();
        dialog.setTitle("退出确认");
        dialog.setHeaderText("请输入账号密码以关闭程序");

        // 设置按钮
        ButtonType exitButtonType = new ButtonType("退出", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(exitButtonType, ButtonType.CANCEL);

        // 创建账号密码输入字段
        TextField usernameField = new TextField();
        usernameField.setPromptText("请输入账号");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");

        // 创建布局
        VBox vbox = new VBox(10);
        vbox.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));
        vbox.getChildren().addAll(
                new Label("请输入账号密码以关闭程序:"),
                new Label("账号:"),
                usernameField,
                new Label("密码:"),
                passwordField
        );

        dialog.getDialogPane().setContent(vbox);

        // 请求焦点到账号输入框
        Platform.runLater(usernameField::requestFocus);

        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == exitButtonType) {
                return new SecurityHandler.UserCredential(
                        usernameField.getText(),
                        passwordField.getText(),
                        SecurityHandler.UserRole.VIEWER // 临时占位符，实际使用中请根据实际需求设置正确的角色.UserRole.VIEWER // 临时占位符
                );
            }
            return null;
        });

        // 显示对话框并处理结果
        Optional<SecurityHandler.UserCredential> result = dialog.showAndWait();
        result.ifPresent(credential -> {
            String username = credential.getUsername();
            String password = credential.getPassword();

            // 验证退出权限
            SecurityHandler.UserCredential validatedUser =
                    securityHandler.validateExitPermission(username, password);
            if (validatedUser != null) {
                log.info("账号密码验证通过，用户 {} 正在退出程序", validatedUser.getUsername());
                performExit(validatedUser.getUsername());
            } else {
                log.warn("账号密码验证失败或无退出权限，账号: {}", username);
                showWarningAlert("验证失败", "输入的账号或密码不正确，或该用户无退出权限");
            }
        });
    }

    /**
     * 执行程序退出
     *
     * @param username 执行退出的用户名
     */
    private void performExit(String username) {
        try {
            // 记录退出操作
            log.info("用户 {} 执行程序退出操作", username);

            // 执行退出前的清理工作
            log.info("正在执行程序退出前的清理工作");

            // 断开PLC连接（如果已连接）
            if (plcService != null && plcService.isConnected()) {
                plcService.disconnect();
                log.info("已断开PLC连接");
            }

            // 用户登出
            securityHandler.logout();

            // 其他清理工作可以在这里添加

            log.info("程序退出，操作用户: {}", username);
            Platform.exit();
            System.exit(0);
        } catch (Exception e) {
            log.error("用户 {} 退出程序时发生错误", username, e);
            Platform.exit();
            System.exit(1);
        }
    }

    private void initializeDataTable() {
        dataTable.setItems(dataRecords);
        log.debug("数据表格初始化完成");
    }

    private void initializeConfigTable() {
        configTable.setItems(tagConfigs);
        log.debug("配置表格初始化完成");
    }

    private void addExampleTagConfigs() {
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

    private void updateChartTagSelector() {
        ObservableList<String> tagNames = FXCollections.observableArrayList();
        for (PlcTagModel tag : tagConfigs) {
            // 布尔值和字符串不适合图表显示
            String dataType = tag.getDataType();
            if (!"BOOL".equals(dataType) && !"STRING".equals(dataType)) {
                tagNames.add(tag.getName());
            }
        }
        chartTagSelector.setItems(tagNames);
        log.debug("更新图表产品选择器，共有 {} 个可选项", tagNames.size());
    }

    @FXML
    private void handleRead() {
        String address = readWriteAddressField.getText().trim();
        if (address.isEmpty()) {
            showWarningAlert("输入错误", "请输入要读取的PLC地址");
            updateStatus("请输入地址");
            return;
        }

        if (plcService == null || !plcService.isConnected()) {
            showWarningAlert("未连接", "请先连接到PLC");
            updateStatus("请先连接PLC");
            return;
        }

        try {
            log.debug("读取PLC地址: {}", address);
            Object value = plcService.read(address);
            readWriteValueField.setText(String.valueOf(value));

            logMessage("读取成功: 地址 " + address + " = " + value);
            updateStatus("读取成功");
            log.info("读取PLC地址 {} 成功，值为: {}", address, value);

        } catch (Exception e) {
            String errorMsg = "读取地址 " + address + " 时出错: " + e.getMessage();
            log.error("读取PLC地址失败: {}", address, e);
            showErrorAlert("读取失败", errorMsg);
            logMessage("读取失败: " + address + " - " + e.getMessage());
            updateStatus("读取失败");
        }
    }

    @FXML
    private void handleWrite() {
        String address = readWriteAddressField.getText().trim();
        String valueStr = readWriteValueField.getText().trim();
        String dataType = dataTypeCombo.getValue();

        if (address.isEmpty() || valueStr.isEmpty()) {
            showWarningAlert("输入错误", "请输入地址和值");
            updateStatus("请输入地址和值");
            return;
        }

        if (plcService == null || !plcService.isConnected()) {
            showWarningAlert("未连接", "请先连接到PLC");
            updateStatus("请先连接PLC");
            return;
        }

        try {
            log.debug("写入PLC地址: {}, 值: {}, 类型: {}", address, valueStr, dataType);
            Object value = convertValue(valueStr, dataType);
            plcService.write(address, value);

            logMessage("写入成功: 地址 " + address + " = " + value);
            updateStatus("写入成功");
            log.info("写入PLC地址 {} 成功，值为: {}", address, value);

        } catch (Exception e) {
            String errorMsg = "写入地址 " + address + " 时出错: " + e.getMessage();
            log.error("写入PLC地址失败: {}", address, e);
            showErrorAlert("写入失败", errorMsg);
            logMessage("写入失败: " + address + " - " + e.getMessage());
            updateStatus("写入失败");
        }
    }

    private Object convertValue(String valueStr, String dataType) throws Exception {
        try {
            switch (dataType) {
                case "BOOL":
                    return Boolean.parseBoolean(valueStr) || "1".equals(valueStr);
                case "BYTE":
                    return Byte.parseByte(valueStr);
                case "INT":
                    return Short.parseShort(valueStr);
                case "DINT":
                    return Integer.parseInt(valueStr);
                case "REAL":
                    return Float.parseFloat(valueStr);
                case "STRING":
                    return valueStr;
                default:
                    throw new IllegalArgumentException("不支持的数据类型: " + dataType);
            }
        } catch (NumberFormatException e) {
            throw new Exception("值 '" + valueStr + "' 不能转换为类型 " + dataType, e);
        }
    }

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
        int count = dataRecords.size();
        dataRecords.clear();
        dataPointCount = 0;
        dataPointCountLabel.setText("0");
        logMessage("已清除 " + count + " 条数据记录");
        updateStatus("已清除数据记录");
        log.info("清除了 {} 条数据记录", count);
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

            dataMonitorService.setRefreshInterval(interval);
            logMessage("刷新间隔已设置为: " + interval + "ms");
            updateStatus("刷新间隔已更新");
            log.info("刷新间隔已设置为: {}ms", interval);

        } catch (NumberFormatException e) {
            showErrorAlert("输入错误", "请输入有效的数字");
            updateStatus("请输入有效数字");
            log.warn("刷新间隔输入格式错误: {}", refreshIntervalField.getText());
        }
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

    @FXML
    private void handleImportConfig() {
        // TODO: 实现配置导入逻辑
        logMessage("导入配置功能尚未实现");
        updateStatus("导入配置功能尚未实现");
        showInfoAlert("提示", "导入配置功能尚未实现");
        log.warn("导入配置功能尚未实现");
    }

    @FXML
    private void handleExportConfig() {
        // TODO: 实现配置导出逻辑
        logMessage("导出配置功能尚未实现");
        updateStatus("导出配置功能尚未实现");
        showInfoAlert("提示", "导出配置功能尚未实现");
        log.warn("导出配置功能尚未实现");
    }

    @FXML
    private void handleSaveConfig() {
        // TODO: 实现配置保存逻辑
        logMessage("保存配置功能尚未实现");
        updateStatus("保存配置功能尚未实现");
        showInfoAlert("提示", "保存配置功能尚未实现");
        log.warn("保存配置功能尚未实现");
    }

    @FXML
    private void handleLoadConfig() {
        // TODO: 实现配置加载逻辑
        logMessage("加载配置功能尚未实现");
        updateStatus("加载配置功能尚未实现");
        showInfoAlert("提示", "加载配置功能尚未实现");
        log.warn("加载配置功能尚未实现");
    }

    @Override
    public void onDataChanged(PlcDataRecord record) {
        Platform.runLater(() -> {
            try {
                // 更新数据点数
                dataPointCount++;
                dataPointCountLabel.setText(String.valueOf(dataPointCount));

                // 更新表格
                updateDataTable(record);

                // 更新图表（仅限数值型数据）
                updateChartData(record);

                // 检查报警条件
                checkAlarms(record);

            } catch (Exception e) {
                log.error("处理数据变更时出错", e);
                logMessage("处理数据变更时出错: " + e.getMessage());
            }
        });
    }

    private void updateDataTable(PlcDataRecord record) {
        boolean found = false;
        for (int i = 0; i < dataRecords.size(); i++) {
            if (dataRecords.get(i).getName().equals(record.getName())) {
                dataRecords.set(i, record);
                found = true;
                break;
            }
        }
        if (!found) {
            dataRecords.add(record);
        }
    }

    private void updateChartData(PlcDataRecord record) {
//        if (chartSeries.containsKey(record.getName()) && record.getValue() instanceof Number) {
//            Number value = (Number) record.getValue();
//            long timeOffset = System.currentTimeMillis() - startTime;
//
//            XYChart.Series<Number, Number> series = chartSeries.get(record.getName());
//            series.getData().add(new XYChart.Data<>(timeOffset / 1000.0, value));
//
//            // 限制图表数据点数量，防止内存占用过大
//            if (series.getData().size() > 100) {
//                series.getData().remove(0);
//            }
//        }
    }

    private void checkAlarms(PlcDataRecord record) {
//        String tagName = record.getName();
//        Double threshold = alarmThresholds.get(tagName);
//
//        if (threshold != null && record.getValue() instanceof Number) {
//            double value = ((Number) record.getValue()).doubleValue();
//            if (value > threshold) {
//                // 检查是否已经存在相同的未确认报警
//                boolean alarmExists = false;
//                for (AlarmRecord alarm : alarmRecords) {
//                    if (!alarm.isAcknowledged() &&
//                            alarm.getType().equals("阈值报警") &&
//                            alarm.getDescription().contains(tagName)) {
//                        alarmExists = true;
//                        break;
//                    }
//                }
//
//                if (!alarmExists) {
//                    AlarmRecord alarm = new AlarmRecord("阈值报警",
//                            tagName + "值过高: " + value + " (阈值: " + threshold + ")");
//                    alarmRecords.add(alarm);
//                    logMessage("阈值报警: " + tagName + " = " + value);
//                    log.warn("阈值报警: {} = {} (阈值: {})", tagName, value, threshold);
//                }
//            }
//        }
    }
}

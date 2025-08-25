package com.yuzj.autolink.plc.control;

import com.yuzj.autolink.dao.model.PlcDataRecord;
import com.yuzj.autolink.dao.model.PlcTagConfig;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 实时数据监控控制器
 *
 * @author yuzj002
 */
@Slf4j
@Component
public class MonitoringControl extends BaseControl {

    // FXML组件引用
    @FXML
    private TextField refreshIntervalField;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> dataTypeFilter;

    @FXML
    private TableView<PlcDataRecord> dataTable;

    @FXML
    private TableColumn<PlcDataRecord, String> productNameColumn;

    @FXML
    private TableColumn<PlcDataRecord, String> addressColumn;

    @FXML
    private TableColumn<PlcDataRecord, String> valueColumn;

    @FXML
    private TableColumn<PlcDataRecord, String> dataTypeColumn;

    @FXML
    private TableColumn<PlcDataRecord, String> qualityColumn;

    @FXML
    private TableColumn<PlcDataRecord, String> timestampColumn;

    @FXML
    private TableColumn<PlcDataRecord, Void> actionColumn;

    // 统计标签引用
    @FXML
    private Label dataPointCountLabel;

    @FXML
    private Label totalTagsCount;

    @FXML
    private Label onlineTagsCount;

    @FXML
    private Label errorDataCount;

    @FXML
    private Label lastRefreshTimeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label systemStatus;

    // 按钮引用
    @FXML
    private ToggleButton autoRefreshToggle;

    // 数据列表
    private final ObservableList<PlcDataRecord> dataRecords = FXCollections.observableArrayList();

    // 时间格式化器
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        // 初始化数据类型筛选器
        dataTypeFilter.setItems(FXCollections.observableArrayList(
                "全部类型", "BOOL", "BYTE", "INT", "DINT", "REAL", "STRING"
        ));
        dataTypeFilter.getSelectionModel().selectFirst();

        // 初始化表格列
        setupTableColumns();

        // 绑定数据到表格
        dataTable.setItems(dataRecords);

        // 初始化统计信息
        updateStatistics();

        // 初始化状态
        updateStatus("监控模块初始化完成");

        log.info("监控控制器初始化完成");
    }

    /**
     * 设置表格列
     */
    private void setupTableColumns() {
        productNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getTagName() != null ?
                                cellData.getValue().getTagName() : ""));

//        addressColumn.setCellValueFactory(cellData ->
//                new javafx.beans.property.SimpleStringProperty(
//                        cellData.getValue().get() != null ?
//                                cellData.getValue().getAddress() : ""));

        valueColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getTagValue() != null ?
                                cellData.getValue().getTagValue() : ""));

        dataTypeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDataType() != null ?
                                cellData.getValue().getDataType() : ""));

        qualityColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getQuality() != null ?
                                cellData.getValue().getQuality() : ""));

        timestampColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreateTime() != null ?
                                cellData.getValue().getCreateTime().format(TIME_FORMATTER) : ""));

        // 操作列设置
        actionColumn.setCellFactory(param -> new TableCell<PlcDataRecord, Void>() {
            private final Button editButton = new Button("编辑");
            private final Button deleteButton = new Button("删除");

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px;");

                editButton.setOnAction(event -> {
                    PlcDataRecord record = getTableView().getItems().get(getIndex());
                    handleEditRecord(record);
                });

                deleteButton.setOnAction(event -> {
                    PlcDataRecord record = getTableView().getItems().get(getIndex());
                    handleDeleteRecord(record);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    @FXML
    private void handleAddTag() {
        Dialog<PlcTagConfig> dialog = new Dialog<>();
        dialog.setTitle("添加监控点");
        dialog.setHeaderText("请输入产品信息");

        // 设置按钮
        ButtonType addButtonType = new ButtonType("添加", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // 创建输入字段
        TextField nameField = new TextField();
        nameField.setPromptText("产品名称");
        TextField addressField = new TextField();
        addressField.setPromptText("PLC地址，如: DB1.DBX0.0");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList("BOOL", "BYTE", "WORD", "DWORD", "INT", "DINT", "REAL", "STRING"));
        typeCombo.getSelectionModel().selectFirst();
        TextField descField = new TextField();
        descField.setPromptText("描述信息");

        // 创建布局
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        grid.add(new Label("产品名称:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("PLC地址:"), 0, 1);
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
                PlcTagConfig tagConfig = new PlcTagConfig();
                tagConfig.setTagName(nameField.getText());
                tagConfig.setAddress(addressField.getText());
                tagConfig.setDataType(typeCombo.getValue());
                tagConfig.setDescription(descField.getText());
                return tagConfig;
            }
            return null;
        });

        Optional<PlcTagConfig> result = dialog.showAndWait();
        result.ifPresent(tagConfig -> {
            tagConfigs.add(tagConfig);
            updateChartTagSelector();
            logMessage("添加了新监控点: " + tagConfig.getTagName());
            updateStatus("已添加监控点: " + tagConfig.getTagName());
            log.info("添加了新监控点: {}", tagConfig.getTagName());

            // 更新统计信息
            updateStatistics();
        });
    }

    /**
     * 编辑记录
     */
    private void handleEditRecord(PlcDataRecord record) {
        showInfoAlert("编辑功能", "编辑功能将在后续版本中实现");
        logMessage("用户尝试编辑记录: " + record.getTagName());
    }

    /**
     * 删除记录
     */
    private void handleDeleteRecord(PlcDataRecord record) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("确定要删除监控点 '" + record.getTagName() + "' 吗？");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dataRecords.remove(record);
                logMessage("已删除监控点: " + record.getTagName());
                updateStatus("已删除监控点");
                updateStatistics();
                log.info("已删除监控点: {}", record.getTagName());
            }
        });
    }

    @FXML
    private void handleClearData() {
        int count = dataRecords.size();
        if (count > 0) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("确认清除");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("确定要清除所有 " + count + " 条监控数据吗？");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    dataRecords.clear();
                    if (dataPointCountLabel != null) {
                        dataPointCountLabel.setText("数据点: 0");
                    }
                    logMessage("已清除 " + count + " 条数据记录");
                    updateStatus("已清除数据记录");
                    updateStatistics();
                    log.info("清除了 {} 条数据记录", count);
                }
            });
        } else {
            showInfoAlert("清除数据", "当前没有数据可以清除");
        }
    }

    @FXML
    private void handleImportTags() {
        showInfoAlert("导入功能", "配置导入功能将在后续版本中实现");
        logMessage("用户尝试导入配置");
    }

    @FXML
    private void handleExportTags() {
        showInfoAlert("导出功能", "配置导出功能将在后续版本中实现");
        logMessage("用户尝试导出配置");
    }

    @FXML
    private void handleRefreshTable() {
        refreshData();
        updateLastRefreshTime();
        logMessage("手动刷新数据表");
        updateStatus("数据表已刷新");
    }

    /**
     * 刷新数据
     */
    private void refreshData() {
        // 在实际应用中，这里应该从PLC或数据库获取最新数据
        // 模拟数据刷新
        updateLastRefreshTime();
        log.debug("刷新监控数据");
    }

    /**
     * 更新最后刷新时间
     */
    private void updateLastRefreshTime() {
        if (lastRefreshTimeLabel != null) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            lastRefreshTimeLabel.setText("最后刷新: " + time);
        }
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        int total = tagConfigs.size();
        int online = (int) tagConfigs.stream().count(); // 模拟在线数量
        int errors = 0; // 模拟错误数量

        if (totalTagsCount != null) {
            totalTagsCount.setText(String.valueOf(total));
        }
        if (onlineTagsCount != null) {
            onlineTagsCount.setText(String.valueOf(online));
        }
        if (errorDataCount != null) {
            errorDataCount.setText(String.valueOf(errors));
        }
        if (dataPointCountLabel != null) {
            dataPointCountLabel.setText("数据点: " + dataRecords.size());
        }
    }

    private void updateChartTagSelector() {
        // 这个方法在基类中可能已经实现
        log.debug("更新图表产品选择器");
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

    /**
     * 更新系统状态显示
     */
    public void updateSystemStatus(String status, boolean isNormal) {
        if (systemStatus != null) {
            systemStatus.setText(status);
            String color = isNormal ? "#27ae60" : "#e74c3c";
            systemStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
    }

    /**
     * 添加监控数据记录
     */
    public void addDataRecord(PlcDataRecord record) {
        Platform.runLater(() -> {
            dataRecords.add(record);
            updateStatistics();
            updateLastRefreshTime();
        });
    }

    /**
     * 更新监控数据记录
     */
    public void updateDataRecord(PlcDataRecord record) {
        Platform.runLater(() -> {
            // 在实际应用中，这里应该更新对应的记录
            updateLastRefreshTime();
        });
    }
}

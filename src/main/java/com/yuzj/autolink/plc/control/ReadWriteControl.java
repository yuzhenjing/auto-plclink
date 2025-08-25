package com.yuzj.autolink.plc.control;

import com.yuzj.autolink.plc.service.PlcService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * PLC读写操作控制器
 *
 * @author yuzj002
 */
@Slf4j
@Component
public class ReadWriteControl extends BaseControl {

    @FXML
    private ComboBox<String> dataTypeCombo;

    // 读写操作组件
    @FXML
    private TextField readWriteAddressField;

    @FXML
    private TextField readWriteValueField;

    // 新增的组件
    @FXML
    private TextArea logArea;

    @Resource
    private PlcService plcService;

    @FXML
    public void initialize() {
        // 初始化数据类型选择
        dataTypeCombo.setItems(FXCollections.observableArrayList(
                "BOOL", "BYTE", "WORD", "DWORD", "INT", "DINT", "REAL", "STRING"
        ));
        dataTypeCombo.getSelectionModel().selectFirst();

        updateStatus("读写操作模块初始化完成");
        log.info("读写操作控制器初始化完成");
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

    /**
     * 处理清空日志
     */
    @FXML
    private void handleClearLog() {
        if (logArea != null) {
            logArea.clear();
            updateStatus("日志已清空");
            log.info("操作日志已清空");
        }
    }

    /**
     * 处理刷新数据类型
     */
    @FXML
    private void handleRefreshTypes() {
        // 可以重新加载数据类型列表
        updateStatus("数据类型列表已刷新");
        logMessage("刷新数据类型列表");
    }

    /**
     * 处理批量读取
     */
    @FXML
    private void handleReadMultiple() {
        showInfoAlert("功能提示", "批量读取功能将在后续版本中实现");
        logMessage("用户尝试使用批量读取功能");
    }

    /**
     * 处理批量写入
     */
    @FXML
    private void handleWriteMultiple() {
        showInfoAlert("功能提示", "批量写入功能将在后续版本中实现");
        logMessage("用户尝试使用批量写入功能");
    }

    private Object convertValue(String valueStr, String dataType) throws Exception {
        try {
            switch (dataType) {
                case "BOOL":
                    return Boolean.parseBoolean(valueStr) || "1".equals(valueStr);
                case "BYTE":
                    return Byte.parseByte(valueStr);
                case "WORD":
                    return Short.parseShort(valueStr);
                case "DWORD":
                    return Integer.parseInt(valueStr);
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
}

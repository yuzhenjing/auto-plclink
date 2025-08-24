package com.yuzj.autolink.plc.controller;

import com.yuzj.autolink.config.PlcProperties;
import com.yuzj.autolink.plc.service.PlcService;
import com.yuzj.autolink.plc.service.PlcServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @author yuzj002
 */
@Slf4j
public class ConnectController extends BaseController {

    // 连接控制组件
    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    public ComboBox<String> protocolCombo;
    @FXML
    private TextField rackField;
    @FXML
    private TextField slotField;
    @FXML
    private TextField timeoutField;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private Label connectionStatus;

    @Resource
    public PlcServiceFactory plcServiceFactory;

    protected PlcService plcService;

    @FXML
    private void handleConnect() {
        try {
            updateStatus("正在连接...");
            log.info("开始连接PLC");

            PlcProperties config = buildConnectionConfig();

            plcService = plcServiceFactory.createService(config);

            plcService.connect();

            // 开始监控配置的产品
            startMonitoringConfiguredTags();

            updateConnectionButtons(true);

            updateConnectionStatus(true, "已连接");

            logMessage("已成功连接到PLC: " + config.getHost() + ":" + config.getPort());
            updateStatus("已连接到PLC");
            log.info("成功连接到PLC: {}:{}", config.getHost(), config.getPort());

        } catch (Exception e) {
            String errorMsg = "连接PLC时出错: " + e.getMessage();
            log.error("连接PLC失败", e);
            showErrorAlert("连接失败", errorMsg);
            logMessage("连接PLC失败: " + e.getMessage());
            updateStatus("连接失败");
        }
    }

    @FXML
    private void handleDisconnect() {
        try {
            updateStatus("正在断开连接...");
            log.info("开始断开PLC连接");

            dataMonitorService.stopMonitoring();
            if (plcService != null && plcService.isConnected()) {
                plcService.disconnect();
            }

            updateConnectionButtons(false);
            updateConnectionStatus(false, "未连接");

            logMessage("已断开与PLC的连接");
            updateStatus("已断开连接");
            log.info("已断开PLC连接");

        } catch (Exception e) {
            String errorMsg = "断开连接时出错: " + e.getMessage();
            log.error("断开PLC连接失败", e);
            showErrorAlert("错误", errorMsg);
            logMessage("断开连接时出错: " + e.getMessage());
            updateStatus("断开连接失败");
        }
    }

    @FXML
    private void handleTestConnection() {
        try {
            updateStatus("测试连接中...");
            updateConnectionStatus(null, "测试中...");
            log.info("开始测试PLC连接");
            PlcProperties config = buildConnectionConfig();
            PlcService testService = plcServiceFactory.createService(config);
            testService.connect();

            // 测试读取一个简单地址
            if ("S7".equalsIgnoreCase(config.getProtocol())) {
                testService.read("DB1.DBX0.0"); // 测试读取一个位
            }

            updateStatus("连接测试成功");
            updateConnectionStatus(null, "测试成功");
            showInfoAlert("测试成功", "PLC连接测试成功");
            logMessage("PLC连接测试成功: " + config.getHost() + ":" + config.getPort());
            log.info("PLC连接测试成功: {}:{}", config.getHost(), config.getPort());

        } catch (Exception e) {
            String errorMsg = "PLC连接测试失败: " + e.getMessage();
            log.error("PLC连接测试失败", e);
            showErrorAlert("测试失败", errorMsg);
            logMessage(errorMsg);
            updateStatus("连接测试失败");
            updateConnectionStatus(false, "测试失败");
        }
    }


    public void updateConnectionButtons(boolean connected) {
        connectButton.setDisable(connected);
        disconnectButton.setDisable(!connected);
        log.debug("更新连接按钮状态: connected={}", connected);
    }

    private void updateConnectionStatus(Boolean connected, String text) {
        connectionStatus.setText(text);

        // 清除所有状态类
        connectionStatus.getStyleClass().removeAll("connected", "disconnected", "connecting");

        // 根据连接状态添加相应样式
        if (connected == null) {
            connectionStatus.getStyleClass().add("connecting");
        } else if (connected) {
            connectionStatus.getStyleClass().add("connected");
        } else {
            connectionStatus.getStyleClass().add("disconnected");
        }

        log.debug("更新连接状态显示: text={}, connected={}", text, connected);
    }


    private PlcProperties buildConnectionConfig() throws NumberFormatException {
        PlcProperties config = new PlcProperties();
        config.setProtocol(protocolCombo.getValue());
        config.setHost(hostField.getText());
        config.setPort(Integer.parseInt(portField.getText()));
        config.setRack(Integer.parseInt(rackField.getText()));
        config.setSlot(Integer.parseInt(slotField.getText()));
        config.setTimeout(Integer.parseInt(timeoutField.getText()));
        return config;
    }

}

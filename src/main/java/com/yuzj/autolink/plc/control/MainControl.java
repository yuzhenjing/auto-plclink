package com.yuzj.autolink.plc.control;

import com.yuzj.autolink.plc.event.PlcConnectStatusEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author yuzj002
 */
@Slf4j
@Component
public class MainControl extends BaseControl {
    @FXML
    private Label latencyLabel;
    @FXML
    private Label dataPointCountLabel;

    @FXML
    public void initialize() {

        // 初始化状态栏
        updateStatus("就绪");

        if (dataPointCountLabel != null) {
            dataPointCountLabel.setText("0");
        }
        if (latencyLabel != null) {
            latencyLabel.setText("0 ms");
        }

        log.info("主控制器初始化完成");
    }

    @EventListener
    public void handleConnectionStatusChange(PlcConnectStatusEvent event) {
        String text = event.isConnected() ? "已连接" : "未连接";
        connectionStatus.setText(text);
        // 清除所有状态类
        connectionStatus.getStyleClass().removeAll("connected", "disconnected", "connecting");
        // 根据连接状态添加相应样式
        if (event.isConnected()) {
            connectionStatus.getStyleClass().add("connected");
        } else {
            connectionStatus.getStyleClass().add("disconnected");
        }
        log.debug("更新连接状态显示: text={}, connected={}", text, event.isConnected());
    }
}
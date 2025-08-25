
package com.yuzj.autolink.plc.control;

import com.yuzj.autolink.dao.PlcDataRecordResoprist;
import com.yuzj.autolink.domain.PlcDataRecord;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author yuzj002
 */
@Slf4j
@Component
public class MainControl extends BaseControl {

    //数据监控组件
    @FXML
    private TableView<PlcDataRecord> dataTable;

    @FXML
    private Label dataPointCountLabel;
    @FXML
    private Label latencyLabel;

    @FXML
    private Label connectionStatus;

    @Resource
    private PlcDataRecordResoprist plcDataRecordResoprist;


    @FXML
    public void initialize() {
        log.info("初始化主控制器");

        // 初始化表格
        initializeDataTable();


        // 初始化状态栏
        updateStatus("就绪");

        if (dataPointCountLabel != null) {
            dataPointCountLabel.setText("0");
        }
        if (latencyLabel != null) {
            latencyLabel.setText("0 ms");
        }
        if (connectionStatus != null) {
            connectionStatus.setText("未连接");
        }

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
                if (dataTable != null && dataTable.getScene() != null && dataTable.getScene().getWindow() != null) {
                    Stage stage = (Stage) dataTable.getScene().getWindow();
                    stage.setOnCloseRequest(this::handleCloseRequest);
                }
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
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("退出确认");
        dialog.setHeaderText("请输入密码以关闭程序");

        // 设置按钮
        ButtonType exitButtonType = new ButtonType("退出", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(exitButtonType, ButtonType.CANCEL);

        // 创建密码输入字段
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");

        // 创建布局
        VBox vbox = new VBox(10);
        vbox.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));
        vbox.getChildren().addAll(
                new Label("请输入密码以关闭程序:"),
                new Label("密码:"),
                passwordField
        );

        dialog.getDialogPane().setContent(vbox);

        // 请求焦点到密码输入框
        Platform.runLater(passwordField::requestFocus);

        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == exitButtonType) {
                return passwordField.getText();
            }
            return null;
        });

        // 显示对话框并处理结果
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            // 验证密码 (简单验证，密码为"123456")
            if ("123456".equals(password)) {
                log.info("密码验证通过，正在退出程序");
                performExit();
            } else {
                log.warn("密码验证失败");
                showWarningAlert("验证失败", "输入的密码不正确");
            }
        });
    }

    /**
     * 执行程序退出
     */
    private void performExit() {
        try {
            // 记录退出操作
            log.info("正在执行程序退出操作");

            // 执行退出前的清理工作
            log.info("正在执行程序退出前的清理工作");

            // 其他清理工作可以在这里添加

            log.info("程序退出");
            Platform.exit();
            System.exit(0);
        } catch (Exception e) {
            log.error("退出程序时发生错误", e);
            Platform.exit();
            System.exit(1);
        }
    }

    private void initializeDataTable() {
        if (plcDataRecordResoprist != null && dataTable != null) {
            dataTable.setItems(plcDataRecordResoprist.list());
            log.debug("数据表格初始化完成");
        }
    }


}
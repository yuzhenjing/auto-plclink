
// src/main/java/com/yuzj/autolink/AutoQrplcLinkApplication.java
package com.yuzj.autolink;

import com.yuzj.autolink.plc.SpringFXMLLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;

@Slf4j
@SpringBootApplication
public class AutoQrplcLinkApplication extends Application {

    private ConfigurableApplicationContext applicationContext;
    private Parent rootNode;

    @Override
    public void init() {
        // 初始化Spring上下文
        applicationContext = new SpringApplicationBuilder()
                .sources(AutoQrplcLinkApplication.class)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            log.info("开始加载FXML界面...");

            // 使用SpringFXMLLoader加载FXML
            SpringFXMLLoader springFXMLLoader = applicationContext.getBean(SpringFXMLLoader.class);

            // 确保FXML文件路径正确
            URL fxmlUrl = getClass().getResource("/fxml/main-view.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("无法找到FXML文件: /fxml/main-view.fxml");
            }

            log.info("FXML文件URL: {}", fxmlUrl);
            rootNode = (Parent) springFXMLLoader.load(fxmlUrl);

            Scene scene = new Scene(rootNode);
            primaryStage.setTitle("PLC 上位机控制系统");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

            log.info("应用启动成功");

        } catch (Exception e) {
            log.error("启动应用时发生错误", e);
            throw e;
        }
    }

    @Override
    public void stop() {
        log.info("Stopping application...");
        if (applicationContext != null) {
            applicationContext.close();
        }
        log.info("Application stopped");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
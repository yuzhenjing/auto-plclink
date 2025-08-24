package com.yuzj.autolink;

import com.yuzj.autolink.plc.SpringFXMLLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JavaFX与Spring Boot集成的PLC HMI上位机软件主应用类
 *
 * @author yuzj002
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
public class AutoQrplcLinkApplication extends Application {

    // 应用程序配置常量
    private static final String APPLICATION_TITLE = "PLC HMI 上位机软件";
    private static final String MAIN_FXML_PATH = "/fxml/main-view.fxml";
    private static final double WINDOW_SIZE_RATIO = 0.8;
    private static final double MIN_WINDOW_WIDTH = 800;
    private static final double MIN_WINDOW_HEIGHT = 600;
    // Spring应用上下文
    private ConfigurableApplicationContext springContext;


    /**
     * 应用程序入口点
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        Application.launch(AutoQrplcLinkApplication.class, args);
    }


    /**
     * 初始化应用程序，启动Spring上下文
     *
     * @throws Exception 初始化过程中可能抛出的异常
     */
    @Override
    public void init() throws Exception {
        log.info("Initializing Spring application context...");
        try {
            // 在JavaFX应用初始化时启动Spring上下文
            springContext = new SpringApplicationBuilder(AutoQrplcLinkApplication.class)
                    .headless(false)
                    .run();
            log.info("Spring application context initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Spring application context", e);
            throw e;
        }
    }

    /**
     * 启动JavaFX应用程序
     *
     * @param primaryStage 主窗口Stage对象
     * @throws Exception 启动过程中可能抛出的异常
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Starting JavaFX application...");
        try {
            // 使用Spring感知的FXML加载器加载主界面
            SpringFXMLLoader loader = springContext.getBean(SpringFXMLLoader.class);
            // JavaFX根节点
            Parent rootNode = loader.load(MAIN_FXML_PATH);

            Scene scene = new Scene(rootNode);

            // 获取屏幕尺寸
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // 设置窗口大小为屏幕的80%
            double windowWidth = screenBounds.getWidth() * WINDOW_SIZE_RATIO;
            double windowHeight = screenBounds.getHeight() * WINDOW_SIZE_RATIO;

            primaryStage.setScene(scene);
            primaryStage.setTitle(APPLICATION_TITLE);
            primaryStage.setWidth(windowWidth);
            primaryStage.setHeight(windowHeight);
            primaryStage.setX((screenBounds.getWidth() - windowWidth) / 2);
            primaryStage.setY((screenBounds.getHeight() - windowHeight) / 2);

            // 允许窗口调整大小
            primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
            primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);

            // 设置关闭请求处理器
            primaryStage.setOnCloseRequest(e -> {
                log.info("Application close requested");
                Platform.exit();
                System.exit(0);
            });

            primaryStage.show();
            log.info("JavaFX application started successfully");
        } catch (Exception e) {
            log.error("Failed to start JavaFX application", e);
            throw e;
        }
    }

    /**
     * 停止应用程序，清理资源
     *
     * @throws Exception 停止过程中可能抛出的异常
     */
    @Override
    public void stop() throws Exception {
        log.info("Stopping application...");
        try {
            // 关闭应用时清理Spring上下文
            if (springContext != null) {
                springContext.close();
                log.info("Spring application context closed");
            }
        } finally {
            Platform.exit();
            log.info("JavaFX platform exited");
        }
    }
}

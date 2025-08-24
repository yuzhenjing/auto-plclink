package com.yuzj.autolink.plc;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author yuzj002
 */
@Component
public class SpringFXMLLoader {

    private final ApplicationContext context;

    public SpringFXMLLoader(ApplicationContext context) {
        this.context = context;
    }

    public Parent load(String fxmlPath) throws IOException {
        try (InputStream fxmlStream = getClass().getResourceAsStream(fxmlPath)) {
            if (fxmlStream == null) {
                throw new IOException("无法找到FXML文件: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(context::getBean);
            return loader.load(fxmlStream);
        }
    }
}
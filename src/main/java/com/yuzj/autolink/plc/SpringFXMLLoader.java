
package com.yuzj.autolink.plc;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.URL;

/**
 * @author yuzj002
 */
@Component
public class SpringFXMLLoader {

    private final ApplicationContext applicationContext;

    public SpringFXMLLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Object load(String fxmlPath) throws IOException {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("无法找到FXML文件: " + fxmlPath);
        }
        return load(fxmlUrl);
    }

    public Object load(URL fxmlUrl) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(fxmlUrl);
        loader.setControllerFactory(applicationContext::getBean);

        // 使用字节流方式加载，手动处理BOM
        try (InputStream inputStream = fxmlUrl.openStream()) {
            InputStream cleanedStream = removeBOM(inputStream);
            return loader.load(cleanedStream);
        }
    }

    /**
     * 移除BOM标记
     */
    private InputStream removeBOM(InputStream inputStream) throws IOException {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream, 3);
        byte[] bom = new byte[3];
        int bytesRead = pushbackInputStream.read(bom);

        if (bytesRead == 3 &&
                bom[0] == (byte) 0xEF &&
                bom[1] == (byte) 0xBB &&
                bom[2] == (byte) 0xBF) {
            // 发现BOM，不推回，相当于移除BOM
            return pushbackInputStream;
        } else {
            // 没有BOM，推回读取的字节
            if (bytesRead > 0) {
                pushbackInputStream.unread(bom, 0, bytesRead);
            }
            return pushbackInputStream;
        }
    }
}
// S7Simulator.java
package com.yuzj.autolink.s7;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yuzj002
 */
@Slf4j
@Component
public class S7Simulator {

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private AtomicBoolean running = new AtomicBoolean(false);
    private S7Memory memory;

    @PostConstruct
    public void init() {
        memory = new S7Memory();
        executorService = Executors.newCachedThreadPool();
    }

    public void start(int port) throws IOException {
        if (running.get()) {
            log.warn("S7模拟器已在运行中");
            return;
        }

        serverSocket = new ServerSocket(port);
        running.set(true);

        log.info("S7模拟器启动，监听端口: {}", port);

        // 启动监听线程
        executorService.submit(this::acceptConnections);
    }

    private void acceptConnections() {
        while (running.get() && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                log.info("新客户端连接: {}", clientSocket.getRemoteSocketAddress());

                // 为每个客户端创建处理线程
                executorService.submit(new S7ClientHandler(clientSocket));
            } catch (IOException e) {
                if (running.get()) {
                    log.error("接受客户端连接时出错", e);
                }
            }
        }
    }

    public void stop() {
        if (!running.get()) {
            log.warn("S7模拟器未运行");
            return;
        }

        running.set(false);

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("关闭服务器套接字时出错", e);
        }

        log.info("S7模拟器已停止");
    }

    @PreDestroy
    public void destroy() {
        stop();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}

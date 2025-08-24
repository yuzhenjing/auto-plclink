// S7ClientHandler.java
package com.yuzj.autolink.s7;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Slf4j
public class S7ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final S7Memory memory;
    private final DataInputStream input;
    private final DataOutputStream output;

    public S7ClientHandler(Socket clientSocket, S7Memory memory) throws IOException {
        this.clientSocket = clientSocket;
        this.memory = memory;
        this.input = new DataInputStream(clientSocket.getInputStream());
        this.output = new DataOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            log.info("开始处理客户端连接: {}", clientSocket.getRemoteSocketAddress());

            byte[] buffer = new byte[1024];
            while (!clientSocket.isClosed() && clientSocket.isConnected()) {
                try {
                    // 读取S7协议数据
                    int bytesRead = input.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }

                    // 解析S7协议并响应
                    byte[] response = handleS7Request(buffer, bytesRead);
                    if (response != null) {
                        output.write(response);
                        output.flush();
                    }
                } catch (IOException e) {
                    if (!clientSocket.isClosed()) {
                        log.error("处理客户端请求时出错", e);
                    }
                    break;
                }
            }
        } finally {
            close();
        }
    }

    private byte[] handleS7Request(byte[] request, int length) {
        try {
            // 简单的S7协议解析和响应
            if (length >= 4) {
                // 检查TPKT头
                if (request[0] == 0x03 && request[1] == 0x00) {
                    // COTP协议数据单元
                    return handleCOTPRequest(request, length);
                }
            }

            // 默认响应
            return createSimpleResponse();
        } catch (Exception e) {
            log.error("处理S7请求时出错", e);
            return null;
        }
    }

    private byte[] handleCOTPRequest(byte[] request, int length) {
        // 简化的COTP处理
        try {
            // 发送连接确认响应
            return createConnectionConfirm();
        } catch (Exception e) {
            log.error("处理COTP请求时出错", e);
            return null;
        }
    }

    private byte[] createConnectionConfirm() {
        // 创建连接确认响应 - 更清晰和可维护的实现
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // TPKT Header (4 bytes)
            baos.write(0x03);  // 版本
            baos.write(0x00);  // 保留
            // 长度字段将在后面设置

            // COTP Connection Confirm (CC) PDU
            baos.write(0x11);  // Length indicator
            baos.write(0xD0);  // CC PDU type
            baos.write(0x00);  // DST-REF (2 bytes)
            baos.write(0x00);
            baos.write(0x00);  // SRC-REF (2 bytes)
            baos.write(0x01);
            baos.write(0x00);  // CLASS + OPTIONS

            // 参数部分
            // TPDU Size (0xC0)
            baos.write(0xC0);
            baos.write(0x01);
            baos.write(0x0A);  // TPDU Size = 1024 bytes (2^10)

            // 最大连接数 (0xC1)
            baos.write(0xC1);
            baos.write(0x02);
            baos.write(0x01);  // 高字节
            baos.write(0x00);  // 低字节 = 256

            // 最大TPDU大小 (0xC2)
            baos.write(0xC2);
            baos.write(0x02);
            baos.write(0x01);  // 高字节
            baos.write(0x01);  // 低字节 = 257

            // 更新TPKT长度字段
            byte[] response = baos.toByteArray();
            int length = response.length;
            response[2] = (byte) ((length >> 8) & 0xFF);
            response[3] = (byte) (length & 0xFF);

            return response;
        } catch (Exception e) {
            log.error("创建连接确认响应时出错", e);
        }
        return null;
    }


    private byte[] createSimpleResponse() {
        // 创建简单的响应
        return new byte[]{0x03, 0x00, 0x00, 0x07, 0x02, (byte) 0xF0, 0x00};
    }

    private void close() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            log.info("客户端连接已关闭: {}", clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            log.error("关闭客户端连接时出错", e);
        }
    }
}

package com.yuzj.autolink.plc.service;

import com.yuzj.autolink.config.PlcProperties;
import com.yuzj.autolink.exception.PlcConnectionException;
import com.yuzj.autolink.plc.service.impl.ModbusRtuOperatorService;
import com.yuzj.autolink.plc.service.impl.ModbusTcpOperatorService;
import com.yuzj.autolink.plc.service.impl.OpcUaOperatorService;
import com.yuzj.autolink.plc.service.impl.S7PlcOperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuzj002
 */
@Component
public class PlcServiceFactory {

    @Autowired
    private S7PlcOperatorService s7PlcService;

    @Autowired
    private ModbusTcpOperatorService modbusTcpService;

    @Autowired
    private ModbusRtuOperatorService modbusRtuService;

    @Autowired
    private OpcUaOperatorService opcUaService;

    public PlcService createService(PlcProperties config) throws PlcConnectionException {
        PlcService service;

        switch (config.getProtocol().toUpperCase()) {
            case "S7":
                service = s7PlcService;
                break;
            case "MODBUS_TCP":
                service = modbusTcpService;
                break;
            case "MODBUS_RTU":
                service = modbusRtuService;
                break;
            case "OPC_UA":
                service = opcUaService;
                break;
            default:
                throw new PlcConnectionException("不支持的PLC协议: " + config.getProtocol());
        }
        service.setConfig(config);
        return service;
    }
}
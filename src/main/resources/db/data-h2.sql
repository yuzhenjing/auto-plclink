-- ================================
-- 插入示例PLC标签配置
-- ================================
INSERT INTO plc_tag_config (tag_name, address, data_type, description)
SELECT 'Motor_Status', 'DB1.DBX0.0', 'BOOL', '电机运行状态'
WHERE NOT EXISTS (SELECT 1 FROM plc_tag_config WHERE tag_name = 'Motor_Status');

INSERT INTO plc_tag_config (tag_name, address, data_type, description)
SELECT 'Temperature', 'DB1.DBD4', 'REAL', '温度值'
WHERE NOT EXISTS (SELECT 1 FROM plc_tag_config WHERE tag_name = 'Temperature');

INSERT INTO plc_tag_config (tag_name, address, data_type, description)
SELECT 'Pressure', 'DB1.DBD8', 'REAL', '压力值'
WHERE NOT EXISTS (SELECT 1 FROM plc_tag_config WHERE tag_name = 'Pressure');

INSERT INTO plc_tag_config (tag_name, address, data_type, description)
SELECT 'Production_Count', 'DB1.DBD12', 'DWORD', '生产计数'
WHERE NOT EXISTS (SELECT 1 FROM plc_tag_config WHERE tag_name = 'Production_Count');

INSERT INTO plc_tag_config (tag_name, address, data_type, description)
SELECT 'Valve_Open', 'DB1.DBX2.0', 'BOOL', '阀门开启状态'
WHERE NOT EXISTS (SELECT 1 FROM plc_tag_config WHERE tag_name = 'Valve_Open');

INSERT INTO plc_tag_config (tag_name, address, data_type, description)
SELECT 'Current_Speed', 'DB1.DBD16', 'REAL', '当前速度'
WHERE NOT EXISTS (SELECT 1 FROM plc_tag_config WHERE tag_name = 'Current_Speed');

INSERT INTO plc_tag_config (tag_name, address, data_type, description)
SELECT 'Error_Code', 'DB1.DBB20', 'BYTE', '错误代码'
WHERE NOT EXISTS (SELECT 1 FROM plc_tag_config WHERE tag_name = 'Error_Code');

-- ================================
-- 插入系统预定义报警类型
-- ================================
INSERT INTO alarm_record (tag_name, alarm_type, alarm_message, alarm_level, alarm_value)
SELECT 'Temperature', 'HIGH_ALARM', '温度过高报警', 'CRITICAL', '85.5'
WHERE NOT EXISTS (SELECT 1 FROM alarm_record WHERE tag_name = 'Temperature' AND alarm_type = 'HIGH_ALARM');

INSERT INTO alarm_record (tag_name, alarm_type, alarm_message, alarm_level, alarm_value)
SELECT 'Pressure', 'LOW_ALARM', '压力过低报警', 'MAJOR', '0.5'
WHERE NOT EXISTS (SELECT 1 FROM alarm_record WHERE tag_name = 'Pressure' AND alarm_type = 'LOW_ALARM');

INSERT INTO alarm_record (tag_name, alarm_type, alarm_message, alarm_level, alarm_value)
SELECT 'Motor_Status', 'DEV_ALARM', '电机故障', 'CRITICAL', 'FALSE'
WHERE NOT EXISTS (SELECT 1 FROM alarm_record WHERE tag_name = 'Motor_Status' AND alarm_type = 'DEV_ALARM');

package com.yuzj.autolink.domain;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @author yuzj002
 */
@Getter
public class PlcDataRecord {


    /**
     * 标签名称属性
     */
    private final StringProperty name = new SimpleStringProperty();

    /**
     * 标签值属性
     */
    private final ObjectProperty<Object> value = new SimpleObjectProperty<>();

    /**
     * 数据类型属性
     */
    private final StringProperty dataType = new SimpleStringProperty();

    /**
     * 时间戳属性
     */
    private final ObjectProperty<LocalDateTime> timestamp = new SimpleObjectProperty<>();

    /**
     * 数据质量属性
     */
    private final StringProperty quality = new SimpleStringProperty("Good");

    /**
     * 默认构造函数
     * 初始化时间戳为当前时间
     */
    public PlcDataRecord() {
        timestamp.set(LocalDateTime.now());
    }

    /**
     * 构造函数
     *
     * @param name     标签名称
     * @param value    标签值
     * @param dataType 数据类型
     */
    public PlcDataRecord(String name, Object value, String dataType) {
        this();
        this.name.set(name);
        this.value.set(value);
        this.dataType.set(dataType);
    }

    // JavaFX Property Getters
    public StringProperty nameProperty() {
        return name;
    }

    public ObjectProperty<Object> valueProperty() {
        return value;
    }

    public StringProperty dataTypeProperty() {
        return dataType;
    }

    public ObjectProperty<LocalDateTime> timestampProperty() {
        return timestamp;
    }

    public StringProperty qualityProperty() {
        return quality;
    }

    // 传统Getters（供MyBatis等使用）
    public String getName() {
        return name.get();
    }

    public Object getValue() {
        return value.get();
    }

    public String getDataType() {
        return dataType.get();
    }

    public LocalDateTime getTimestamp() {
        return timestamp.get();
    }

    public String getQuality() {
        return quality.get();
    }

    // 传统Setters（供MyBatis等使用）
    public void setName(String name) {
        this.name.set(name);
    }

    public void setValue(Object value) {
        this.value.set(value);
    }

    public void setDataType(String dataType) {
        this.dataType.set(dataType);
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp.set(timestamp);
    }

    public void setQuality(String quality) {
        this.quality.set(quality);
    }

    @Override
    public String toString() {
        return "PlcTagData{" +
                "name='" + getName() + '\'' +
                ", value=" + getValue() +
                ", dataType='" + getDataType() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", quality='" + getQuality() + '\'' +
                '}';
    }
}
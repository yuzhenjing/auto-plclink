
package com.yuzj.autolink.domain;

import javafx.beans.property.*;
import lombok.Getter;

/**
 * PLC产品配置属性
 * 用于定义PLC数据产品的配置信息，支持JavaFX属性绑定
 *
 * @author yuzj002
 */
@Getter
public class PlcTagModel {

    /**
     * 产品名称属性
     */
    private final StringProperty name = new SimpleStringProperty();

    /**
     * 产品地址属性
     */
    private final StringProperty address = new SimpleStringProperty();

    /**
     * 数据类型属性
     */
    private final StringProperty dataType = new SimpleStringProperty();

    /**
     * 产品描述属性
     */
    private final StringProperty description = new SimpleStringProperty();

    /**
     * 采样间隔(毫秒)
     */
    private final IntegerProperty samplingInterval = new SimpleIntegerProperty(100);

    /**
     * 是否启用
     */
    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

    /**
     * 构造函数 - 基本信息
     *
     * @param name     产品名称
     * @param address  产品地址
     * @param dataType 数据类型
     */
    public PlcTagModel(String name, String address, String dataType) {
        this.name.set(name);
        this.address.set(address);
        this.dataType.set(dataType);
    }

    /**
     * 构造函数 - 包含描述信息
     *
     * @param name        产品名称
     * @param address     产品地址
     * @param dataType    数据类型
     * @param description 产品描述
     */
    public PlcTagModel(String name, String address, String dataType, String description) {
        this(name, address, dataType);
        this.description.set(description);
    }

    // JavaFX Property Getters
    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty addressProperty() {
        return address;
    }

    public StringProperty dataTypeProperty() {
        return dataType;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public IntegerProperty samplingIntervalProperty() {
        return samplingInterval;
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    // 传统Getters（供MyBatis等使用）
    public String getName() {
        return name.get();
    }

    public String getAddress() {
        return address.get();
    }

    public String getDataType() {
        return dataType.get();
    }

    public String getDescription() {
        return description.get();
    }

    public int getSamplingInterval() {
        return samplingInterval.get();
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    // 传统Setters（供MyBatis等使用）
    public void setName(String name) {
        this.name.set(name);
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public void setDataType(String dataType) {
        this.dataType.set(dataType);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setSamplingInterval(int samplingInterval) {
        this.samplingInterval.set(samplingInterval);
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    @Override
    public String toString() {
        return "PlcTagProperties{" +
                "name=" + getName() +
                ", address=" + getAddress() +
                ", dataType=" + getDataType() +
                ", description=" + getDescription() +
                ", samplingInterval=" + getSamplingInterval() +
                ", enabled=" + isEnabled() +
                '}';
    }
}
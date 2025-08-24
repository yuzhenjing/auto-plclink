package com.yuzj.autolink.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 报警记录实体类
 * 用于存储和管理PLC系统中的报警信息
 */
public class AlarmRecord {
    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StringProperty time = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty("未确认");
    private final StringProperty priority = new SimpleStringProperty("中");
    /**
     * -- GETTER --
     *  获取报警是否已确认
     *
     * @return true表示已确认，false表示未确认
     */
    @Getter
    private boolean acknowledged = false;
    private final LocalDateTime createdTime;

    /**
     * 默认构造函数
     * 自动生成当前时间作为报警时间
     */
    public AlarmRecord() {
        this.createdTime = LocalDateTime.now();
        this.time.set(this.createdTime.format(DEFAULT_FORMATTER));
    }

    /**
     * 带参数的构造函数
     *
     * @param type        报警类型
     * @param description 报警描述
     */
    public AlarmRecord(String type, String description) {
        this();
        this.type.set(Objects.requireNonNull(type, "报警类型不能为空"));
        this.description.set(Objects.requireNonNull(description, "报警描述不能为空"));
    }

    /**
     * 完整参数构造函数
     *
     * @param type        报警类型
     * @param description 报警描述
     * @param priority    报警优先级
     */
    public AlarmRecord(String type, String description, String priority) {
        this(type, description);
        this.priority.set(Objects.requireNonNull(priority, "报警优先级不能为空"));
    }

    /**
     * 确认报警
     */
    public void acknowledge() {
        this.status.set("已确认");
        this.acknowledged = true;
    }

    /**
     * 获取报警创建时间
     *
     * @return 报警创建的LocalDateTime对象
     */
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    /**
     * 更新报警时间
     *
     * @param time 时间
     */
    public void setTime(LocalDateTime time) {
        Objects.requireNonNull(time, "时间不能为空");
        this.time.set(time.format(DEFAULT_FORMATTER));
    }

    /**
     * 更新报警时间字符串
     *
     * @param time 时间字符串
     */
    public void setTime(String time) {
        Objects.requireNonNull(time, "时间不能为空");
        this.time.set(time);
    }

    /**
     * 设置报警类型
     *
     * @param type 报警类型
     */
    public void setType(String type) {
        this.type.set(Objects.requireNonNull(type, "报警类型不能为空"));
    }

    /**
     * 设置报警描述
     *
     * @param description 报警描述
     */
    public void setDescription(String description) {
        this.description.set(Objects.requireNonNull(description, "报警描述不能为空"));
    }

    /**
     * 设置报警状态
     *
     * @param status 报警状态
     */
    public void setStatus(String status) {
        this.status.set(Objects.requireNonNull(status, "报警状态不能为空"));
    }

    /**
     * 设置报警优先级
     *
     * @param priority 报警优先级
     */
    public void setPriority(String priority) {
        this.priority.set(Objects.requireNonNull(priority, "报警优先级不能为空"));
    }

    // 属性获取方法
    public StringProperty timeProperty() {
        return time;
    }

    public StringProperty typeProperty() {
        return type;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty priorityProperty() {
        return priority;
    }

    // 值获取方法
    public String getTime() {
        return time.get();
    }

    public String getType() {
        return type.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getStatus() {
        return status.get();
    }

    public String getPriority() {
        return priority.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AlarmRecord that = (AlarmRecord) o;
        return Objects.equals(getTime(), that.getTime()) &&
                Objects.equals(getType(), that.getType()) &&
                Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), getType(), getDescription());
    }

    @Override
    public String toString() {
        return "AlarmRecord{" +
                "time=" + getTime() +
                ", type='" + getType() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", priority='" + getPriority() + '\'' +
                ", acknowledged=" + acknowledged +
                '}';
    }
}

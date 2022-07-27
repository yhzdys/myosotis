package com.yhzdys.myosotis.entity;

import com.yhzdys.myosotis.enums.EventType;

/**
 * myosotis config change event
 */
public final class MyosotisEvent {

    private Long id;
    private String namespace;
    private String configKey;
    private String configValue;
    private Integer version;
    private EventType type;

    public MyosotisEvent() {
    }

    public MyosotisEvent(String namespace, String configKey, EventType type) {
        this.id = 0L;
        this.namespace = namespace;
        this.configKey = configKey;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public MyosotisEvent setId(Long id) {
        this.id = id;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public MyosotisEvent setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getConfigKey() {
        return configKey;
    }

    public MyosotisEvent setConfigKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    public String getConfigValue() {
        return configValue;
    }

    public MyosotisEvent setConfigValue(String configValue) {
        this.configValue = configValue;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public MyosotisEvent setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public EventType getType() {
        return type;
    }

    public MyosotisEvent setType(EventType type) {
        this.type = type;
        return this;
    }
}

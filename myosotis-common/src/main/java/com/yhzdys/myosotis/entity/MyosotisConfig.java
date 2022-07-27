package com.yhzdys.myosotis.entity;

/**
 * myosotis config entity
 */
public final class MyosotisConfig {

    private Long id;
    private String namespace;
    private String configKey;
    private String configValue;
    private Integer version;

    public MyosotisConfig() {
    }

    public Long getId() {
        return id;
    }

    public MyosotisConfig setId(Long id) {
        this.id = id;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public MyosotisConfig setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getConfigKey() {
        return configKey;
    }

    public MyosotisConfig setConfigKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    public String getConfigValue() {
        return configValue;
    }

    public MyosotisConfig setConfigValue(String configValue) {
        this.configValue = configValue;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public MyosotisConfig setVersion(Integer version) {
        this.version = version;
        return this;
    }
}

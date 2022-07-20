package com.yhzdys.myosotis.web.entity.vo;

import com.yhzdys.myosotis.database.object.MyosotisConfigDO;

public class ConfigVO {

    private Long id;

    private String namespace;

    private String configKey;

    private String description;

    private String configValue;

    public ConfigVO convert(MyosotisConfigDO config) {
        this.id = config.getId();
        this.namespace = config.getNamespace();
        this.configKey = config.getConfigKey();
        this.description = config.getDescription();
        this.configValue = config.getConfigValue();
        return this;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

}

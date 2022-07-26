package com.yhzdys.myosotis;

import com.yhzdys.myosotis.data.CachedConfigData;

import java.math.BigDecimal;

/**
 * facade of myosotis configs
 */
public final class MyosotisClient {

    private final String namespace;

    private final CachedConfigData cachedConfigData;

    MyosotisClient(String namespace, CachedConfigData cachedConfigData) {
        this.namespace = namespace;
        this.cachedConfigData = cachedConfigData;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getString(String configKey) {
        return cachedConfigData.get(namespace, configKey);
    }

    public <T> T get(String configKey, Parser<T> parser) {
        String configValue = this.getString(configKey);
        return configValue == null ? null : parser.parse(configValue);
    }

    public Integer getInteger(String configKey) {
        return this.get(configKey, Integer::parseInt);
    }

    public Long getLong(String configKey) {
        return this.get(configKey, Long::parseLong);
    }

    public Float getFloat(String configKey) {
        return this.get(configKey, Float::parseFloat);
    }

    public Double getDouble(String configKey) {
        return this.get(configKey, Double::parseDouble);
    }

    public Boolean getBoolean(String configKey) {
        return this.get(configKey, Boolean::parseBoolean);
    }

    public BigDecimal getDecimal(String configKey) {
        return this.get(configKey, BigDecimal::new);
    }
}
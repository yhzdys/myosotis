package com.yhzdys.myosotis;

import com.yhzdys.myosotis.data.CachedConfigs;

import java.math.BigDecimal;

/**
 * facade of myosotis configs
 */
public final class MyosotisClient {

    private final String namespace;

    private final CachedConfigs cachedConfigs;

    MyosotisClient(String namespace, CachedConfigs cachedConfigs) {
        this.namespace = namespace;
        this.cachedConfigs = cachedConfigs;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getString(String configKey) {
        return cachedConfigs.get(namespace, configKey);
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
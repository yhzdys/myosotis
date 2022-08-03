package com.yhzdys.myosotis;

import com.yhzdys.myosotis.data.CachedConfig;

import java.math.BigDecimal;

public final class MyosotisClient {

    private final String namespace;

    private final CachedConfig cachedConfig;

    MyosotisClient(String namespace, CachedConfig cachedConfig) {
        this.namespace = namespace;
        this.cachedConfig = cachedConfig;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getString(String configKey) {
        return cachedConfig.get(namespace, configKey);
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

    public BigDecimal getBigDecimal(String configKey) {
        return this.get(configKey, BigDecimal::new);
    }
}

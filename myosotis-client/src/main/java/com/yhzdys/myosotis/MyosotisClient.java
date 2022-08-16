package com.yhzdys.myosotis;

import java.math.BigDecimal;

public final class MyosotisClient {

    private final String namespace;

    private final MyosotisApplication application;

    MyosotisClient(MyosotisApplication application, String namespace) {
        this.application = application;
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getString(String configKey) {
        return application.getConfig(namespace, configKey);
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

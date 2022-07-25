package com.yhzdys.myosotis;

import com.yhzdys.myosotis.data.CachedData;

/**
 * facade of myosotis configs
 */
public final class MyosotisClient {

    private final String namespace;

    private final CachedData cachedData;

    MyosotisClient(String namespace, CachedData cachedData) {
        this.namespace = namespace;
        this.cachedData = cachedData;
    }

    public String getConfig(String configKey) {
        return cachedData.get(namespace, configKey);
    }

    public String getConfig(String configKey, String defaultValue) {
        String configValue = cachedData.get(namespace, configKey);
        return configValue == null ? defaultValue : configValue;
    }

    public String getNamespace() {
        return namespace;
    }
}
package com.yhzdys.myosotis.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CachedConfig {

    /**
     * <namespace, <configKey, configValue>>
     */
    private final Map<String, Map<String, String>> configs = new ConcurrentHashMap<>(2);

    public void add(String namespace) {
        configs.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>(2));
    }

    public void add(String namespace, String configKey, String configValue) {
        configs.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>(2)).put(configKey, configValue);
    }

    public void remove(String namespace, String configKey) {
        Map<String, String> configs = this.configs.get(namespace);
        if (configs == null) {
            return;
        }
        configs.remove(configKey);
        if (configs.isEmpty()) {
            this.configs.remove(namespace);
        }
    }

    public String get(String namespace, String configKey) {
        Map<String, String> configs = this.configs.get(namespace);
        if (configs == null) {
            return null;
        }
        return configs.get(configKey);
    }
}

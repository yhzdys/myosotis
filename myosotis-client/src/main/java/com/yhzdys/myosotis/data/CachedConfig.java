package com.yhzdys.myosotis.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CachedConfig {

    /**
     * <namespace, <configKey, configValue>>
     */
    private final Map<String, Map<String, String>> configMap = new ConcurrentHashMap<>(2);

    public void add(String namespace) {
        configMap.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>(2));
    }

    public void add(String namespace, String configKey, String configValue) {
        Map<String, String> configs = configMap.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>(2));
        configs.put(configKey, configValue);
    }

    public void remove(String namespace, String configKey) {
        Map<String, String> configs = configMap.get(namespace);
        if (configs == null) {
            return;
        }
        configs.remove(configKey);
    }

    public String get(String namespace, String configKey) {
        Map<String, String> configs = configMap.get(namespace);
        if (configs == null) {
            return null;
        }
        return configs.get(configKey);
    }
}

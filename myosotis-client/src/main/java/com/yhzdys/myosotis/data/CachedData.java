package com.yhzdys.myosotis.data;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * cached configs from local file & server
 */
public final class CachedData {

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

    public Set<String> getNamespaces() {
        return Collections.unmodifiableSet(configMap.keySet());
    }

    public boolean containsNamespaceConfig(String namespace) {
        Map<String, String> configs = configMap.get(namespace);
        return configs != null && !configs.isEmpty();
    }
}

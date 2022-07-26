package com.yhzdys.myosotis.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * cached configs from local file and server
 */
public final class CachedConfig {

    /**
     * <namespace, <configKey, configValue>>
     */
    private final Map<String, Map<String, String>> configMap = new ConcurrentHashMap<>(2);

    /**
     * init namespace configs cache
     *
     * @param namespace namespace
     */
    public void add(String namespace) {
        configMap.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>(2));
    }

    /**
     * add cached config
     *
     * @param namespace   namespace
     * @param configKey   configKey
     * @param configValue configValue
     */
    public void add(String namespace, String configKey, String configValue) {
        Map<String, String> configs = configMap.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>(2));
        configs.put(configKey, configValue);
    }

    /**
     * remove cached config
     *
     * @param namespace namespace
     * @param configKey configKey
     */
    public void remove(String namespace, String configKey) {
        Map<String, String> configs = configMap.get(namespace);
        if (configs == null) {
            return;
        }
        configs.remove(configKey);
    }

    /**
     * get cached config value
     *
     * @param namespace namespace
     * @param configKey configKey
     * @return configValue
     */
    public String get(String namespace, String configKey) {
        Map<String, String> configs = configMap.get(namespace);
        if (configs == null) {
            return null;
        }
        return configs.get(configKey);
    }

    /**
     * namespace contains config
     *
     * @param namespace namespace
     * @return result
     */
    public boolean containsNamespaceConfig(String namespace) {
        Map<String, String> configs = configMap.get(namespace);
        return configs != null && !configs.isEmpty();
    }
}

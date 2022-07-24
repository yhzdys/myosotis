package com.yhzdys.myosotis.data;

import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * metadata of absent config (the configuration that does not exist in the server)
 *
 * @see com.yhzdys.myosotis.processor.ServerProcessor#getConfig(String, String)
 */
public final class AbsentConfigData {

    private final Object emptyObject = new Object();

    /**
     * <namespace, <configKey, Object.class>>
     */
    private final Map<String, Map<String, Object>> configMap = new ConcurrentHashMap<>(0);

    public boolean isAbsent(String namespace, String configKey) {
        Map<String, Object> absentConfigMap = configMap.computeIfAbsent(namespace, n -> new ConcurrentHashMap<>(2));
        return absentConfigMap.containsKey(configKey);
    }

    public void add(String namespace, String configKey) {
        Map<String, Object> absentKeyMap = configMap.computeIfAbsent(namespace, n -> new ConcurrentHashMap<>(2));
        absentKeyMap.put(configKey, emptyObject);
    }

    public void remove(String namespace, String configKey) {
        Map<String, Object> absentKeyMap = configMap.get(namespace);
        if (MapUtils.isEmpty(absentKeyMap)) {
            return;
        }
        absentKeyMap.remove(configKey);
    }

    public void clear() {
        configMap.clear();
    }

}
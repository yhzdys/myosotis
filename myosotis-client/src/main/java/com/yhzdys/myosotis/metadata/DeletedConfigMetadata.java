package com.yhzdys.myosotis.metadata;

import com.yhzdys.myosotis.event.listener.ConfigListener;
import org.apache.commons.collections4.MapUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * metadata of deleted config (the configuration that not exist in the server but exist the configListeners)
 *
 * @see com.yhzdys.myosotis.MyosotisClientManager#addConfigListener(ConfigListener)
 */
public final class DeletedConfigMetadata {

    /**
     * <namespace, <configKey , id>>
     */
    private final Map<String, Map<String, Long>> cacheMap = new ConcurrentHashMap<>(0);

    public void add(Long id, String namespace, String configKey) {
        Map<String, Long> keyIdMap = cacheMap.computeIfAbsent(namespace, n -> new ConcurrentHashMap<>(2));
        keyIdMap.put(configKey, id);
    }

    public void remove(String namespace, String configKey) {
        Map<String, Long> keyMap = cacheMap.get(namespace);
        if (keyMap == null) {
            return;
        }
        keyMap.remove(configKey);
        if (MapUtils.isEmpty(keyMap)) {
            cacheMap.remove(namespace);
        }
    }

    public Map<String, Map<String, Long>> getMap() {
        return Collections.unmodifiableMap(cacheMap);
    }
}

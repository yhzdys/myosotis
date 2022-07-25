package com.yhzdys.myosotis.data;

import com.yhzdys.myosotis.MyosotisApplication;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.misc.LoggerFactory;
import org.apache.commons.collections4.MapUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * metadata of deleted config (the configuration that not exist in the server but exist the configListeners)
 *
 * @see MyosotisApplication#addConfigListener(ConfigListener)
 */
public final class DeletedMetadata {

    /**
     * <namespace, <configKey , id>>
     */
    private final Map<String, Map<String, Long>> configMap = new ConcurrentHashMap<>(0);

    private int count = 0;

    public void add(Long id, String namespace, String configKey) {
        Map<String, Long> keyIdMap = configMap.computeIfAbsent(namespace, n -> new ConcurrentHashMap<>(2));
        keyIdMap.put(configKey, id);
        count++;
        if (count > 10) {
            LoggerFactory.getLogger().warn("There are more than 10 deleted configs.");
        }
    }

    public void remove(String namespace, String configKey) {
        Map<String, Long> keyMap = configMap.get(namespace);
        if (keyMap == null) {
            return;
        }
        Long id = keyMap.remove(configKey);
        if (id != null) {
            count--;
        }
        if (MapUtils.isEmpty(keyMap)) {
            configMap.remove(namespace);
        }
    }

    public Map<String, Map<String, Long>> getMap() {
        return Collections.unmodifiableMap(configMap);
    }
}

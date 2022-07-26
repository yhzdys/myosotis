package com.yhzdys.myosotis.data;

import com.yhzdys.myosotis.misc.LoggerFactory;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * metadata of absent config (the configuration that does not exist in the server)
 *
 * @see com.yhzdys.myosotis.processor.ServerProcessor#getConfig(String, String)
 */
public final class AbsentConfigMetadata {

    /**
     * <namespace, <configKey, Object.class>>
     */
    private final Map<String, Map<String, Object>> configMap = new ConcurrentHashMap<>(0);

    private final Object emptyObject = new Object();
    /**
     * threshold of clear absent config cache (ms.)
     */
    private final long threshold = TimeUnit.MINUTES.toMillis(10);
    private final AtomicInteger counter = new AtomicInteger(0);
    private long lastClearTime = 0L;

    public boolean isAbsent(String namespace, String configKey) {
        Map<String, Object> absentConfigMap = configMap.computeIfAbsent(namespace, n -> new ConcurrentHashMap<>(2));
        return absentConfigMap.containsKey(configKey);
    }

    public void add(String namespace, String configKey) {
        Map<String, Object> absentKeyMap = configMap.computeIfAbsent(namespace, n -> new ConcurrentHashMap<>(2));
        absentKeyMap.put(configKey, emptyObject);
        int count = counter.incrementAndGet();
        if (count > 10) {
            LoggerFactory.getLogger().warn("There are more than 10 absent configs.");
        }
    }

    public void remove(String namespace, String configKey) {
        Map<String, Object> absentKeyMap = configMap.get(namespace);
        if (MapUtils.isEmpty(absentKeyMap)) {
            return;
        }
        Object object = absentKeyMap.remove(configKey);
        if (object != null) {
            counter.decrementAndGet();
        }
    }

    public void clear() {
        long now = System.currentTimeMillis();
        if ((now - lastClearTime) < threshold) {
            return;
        }
        lastClearTime = now;
        configMap.clear();
    }

}

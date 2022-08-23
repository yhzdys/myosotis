package com.yhzdys.myosotis.data;

import com.yhzdys.myosotis.entity.PollingData;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class ConfigMetadata {

    /**
     * <namespace, PollingData.class>
     */
    private final Map<String, PollingData> pollingConfigs = new ConcurrentHashMap<>(2);
    private final AtomicLong version = new AtomicLong(1L);

    /**
     * <namespace, Set<configKey>>
     */
    private final Map<String, Set<String>> absentConfigs = new ConcurrentHashMap<>(0);
    /**
     * intervals of clear absent config(s)
     */
    private final long threshold = TimeUnit.MINUTES.toMillis(1);
    private long lastClearTime = 0L;

    public long getVersion() {
        return version.get();
    }

    public Collection<PollingData> pollingData() {
        return Collections.unmodifiableCollection(pollingConfigs.values());
    }

    public boolean isPollingAll(String namespace) {
        return this.getPollingData(namespace).isAll();
    }

    public void setPollingAll(String namespace) {
        this.getPollingData(namespace).setAll(true);
    }

    public void addPolling(String namespace, String configKey, Integer version) {
        this.getPollingData(namespace).getData().put(configKey, version);
        this.updateVersion();
    }

    public void removePolling(String namespace, String configKey) {
        PollingData pollingData = pollingConfigs.get(namespace);
        if (pollingData == null) {
            return;
        }
        pollingData.getData().remove(configKey);
        this.updateVersion();
    }

    public boolean isAbsent(String namespace, String configKey) {
        Set<String> configs = absentConfigs.get(namespace);
        return CollectionUtils.isNotEmpty(configs) && configs.contains(configKey);
    }

    public void addAbsent(String namespace, String configKey) {
        absentConfigs.computeIfAbsent(namespace, n -> new CopyOnWriteArraySet<>()).add(configKey);
    }

    public void removeAbsent(String namespace, String configKey) {
        Set<String> configs = absentConfigs.get(namespace);
        if (configs == null) {
            return;
        }
        configs.remove(configKey);
        if (configs.isEmpty()) {
            absentConfigs.remove(namespace);
        }
    }

    public void clearAbsent() {
        long now = System.currentTimeMillis();
        if ((now - lastClearTime) < threshold) {
            return;
        }
        lastClearTime = now;
        absentConfigs.clear();
    }

    private PollingData getPollingData(String namespace) {
        return pollingConfigs.computeIfAbsent(namespace, k -> new PollingData(false, namespace, new ConcurrentHashMap<>(2)));
    }

    private void updateVersion() {
        version.incrementAndGet();
    }
}

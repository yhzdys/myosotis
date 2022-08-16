package com.yhzdys.myosotis.data;

import com.yhzdys.myosotis.entity.PollingData;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class ConfigMetadata {

    private final AtomicLong pollingVersion = new AtomicLong(1L);

    /**
     * <namespace, PollingData.class>
     */
    private final Map<String, PollingData> pollingConfigs = new ConcurrentHashMap<>(2);

    /**
     * <namespace, Set<configKey>>
     */
    private final Map<String, Set<String>> absentConfigs = new ConcurrentHashMap<>(0);

    /**
     * threshold of clear absent config cache (ms.)
     */
    private final long threshold = TimeUnit.MINUTES.toMillis(1);
    private long lastClearTime = 0L;

    public Collection<PollingData> pollingData() {
        return Collections.unmodifiableCollection(pollingConfigs.values());
    }

    public boolean isPollingAll(String namespace) {
        PollingData pollingData = this.getPollingData(namespace);
        return pollingData.isAll();
    }

    public void setPollingAll(String namespace) {
        this.getPollingData(namespace).setAll(true);
    }

    private void updatePollingVersion() {
        pollingVersion.incrementAndGet();
    }

    public void addPolling(String namespace, String configKey, Integer version) {
        this.getPollingData(namespace).getData().put(configKey, version);
        this.updatePollingVersion();
    }

    public void removePolling(String namespace, String configKey) {
        PollingData pollingData = pollingConfigs.get(namespace);
        if (pollingData == null) {
            return;
        }
        pollingData.getData().remove(configKey);
        this.updatePollingVersion();
    }

    public long pollingVersion() {
        return pollingVersion.get();
    }

    private PollingData getPollingData(String namespace) {
        return pollingConfigs.computeIfAbsent(
                namespace, cg -> new PollingData(false, namespace, new ConcurrentHashMap<>(2))
        );
    }

    public boolean isAbsent(String namespace, String configKey) {
        Set<String> configs = absentConfigs.get(namespace);
        if (configs == null || configs.isEmpty()) {
            return false;
        }
        return configs.contains(configKey);
    }

    public void addAbsent(String namespace, String configKey) {
        absentConfigs.computeIfAbsent(namespace, n -> new CopyOnWriteArraySet<>())
                .add(configKey);
    }

    public void removeAbsent(String namespace, String configKey) {
        Set<String> configs = absentConfigs.get(namespace);
        if (configs == null) {
            return;
        }
        if (configs.isEmpty()) {
            absentConfigs.remove(namespace);
            return;
        }
        configs.remove(configKey);
    }

    public void clearAbsent() {
        long now = System.currentTimeMillis();
        if ((now - lastClearTime) < threshold) {
            return;
        }
        lastClearTime = now;
        absentConfigs.clear();
    }
}

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

/**
 * metadata of configs (polling、absent、deleted)
 */
public final class ConfigMetadata {

    /**
     * polling data version
     */
    private final AtomicLong pollingVersion = new AtomicLong(1L);

    /**
     * medata of polling configs
     * <namespace, PollingData.class>
     */
    private final Map<String, PollingData> pollingDataMap = new ConcurrentHashMap<>(2);

    /**
     * medata of absent configs
     * <namespace, Set<configKey>>
     */
    private final Map<String, Set<String>> absentConfigs = new ConcurrentHashMap<>(0);
    /**
     * threshold of clear absent config cache (ms.)
     */
    private final long threshold = TimeUnit.MINUTES.toMillis(1);
    /**
     * medata of deleted configs
     * <namespace, Set<configKey>>
     */
    private final Map<String, Set<String>> deletedConfigs = new ConcurrentHashMap<>(0);
    private long lastClearTime = 0L;

    /**
     * confirm namespace configs in polling data
     *
     * @param namespace namespace
     * @return result
     */
    public boolean inPolling(String namespace) {
        PollingData pollingData = pollingDataMap.get(namespace);
        return pollingData != null;
    }

    /**
     * get all pollingData
     *
     * @return pollingData
     */
    public Collection<PollingData> pollingData() {
        return Collections.unmodifiableCollection(pollingDataMap.values());
    }

    /**
     * set namespace to polling all configs
     *
     * @param namespace namespace
     */
    public void setPollingAll(String namespace) {
        this.getPollingData(namespace).setAll(true);
    }

    private void updatePollingVersion() {
        pollingVersion.incrementAndGet();
    }

    /**
     * add config data for polling
     *
     * @param namespace namespace
     * @param configKey configKey
     * @param version   version
     */
    public void addPolling(String namespace, String configKey, Integer version) {
        this.getPollingData(namespace).getData().put(configKey, version);
        this.updatePollingVersion();
    }

    /**
     * remove config
     *
     * @param namespace namespace
     * @param configKey configKey
     */
    public void removePolling(String namespace, String configKey) {
        PollingData pollingData = pollingDataMap.get(namespace);
        if (pollingData == null) {
            return;
        }
        pollingData.getData().remove(configKey);
        this.updatePollingVersion();
    }

    /**
     * update polling config data
     *
     * @param namespace namespace
     * @param configKey configKey
     * @param version   version
     */
    public void updatePolling(String namespace, String configKey, Integer version) {
        this.getPollingData(namespace)
                .getData().put(configKey, version);
        this.updatePollingVersion();
    }

    /**
     * get polling data version
     *
     * @return current polling version
     */
    public long pollingVersion() {
        return pollingVersion.get();
    }

    private PollingData getPollingData(String namespace) {
        return pollingDataMap.computeIfAbsent(
                namespace, cg -> new PollingData(false, namespace, new ConcurrentHashMap<>(2))
        );
    }

    /**
     * @param namespace namespace
     * @param configKey configKey
     * @return {@code true} isAbsent {@code false} existed config
     */
    public boolean isAbsent(String namespace, String configKey) {
        Set<String> configs = absentConfigs.get(namespace);
        if (configs == null || configs.isEmpty()) {
            return false;
        }
        return configs.contains(configKey);
    }

    /**
     * add absent config
     *
     * @param namespace namespace
     * @param configKey configKey
     */
    public void addAbsent(String namespace, String configKey) {
        absentConfigs.computeIfAbsent(namespace, n -> new CopyOnWriteArraySet<>())
                .add(configKey);
    }

    /**
     * remove absent config
     *
     * @param namespace namespace
     * @param configKey configKey
     */
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

    /**
     * clear all absent configs
     */
    public void clearAbsent() {
        long now = System.currentTimeMillis();
        if ((now - lastClearTime) < threshold) {
            return;
        }
        lastClearTime = now;
        absentConfigs.clear();
    }

    /**
     * add deleted config
     *
     * @param namespace namespace
     * @param configKey configKey
     */
    public void addDeleted(String namespace, String configKey) {
        deletedConfigs.computeIfAbsent(namespace, n -> new CopyOnWriteArraySet<>())
                .add(configKey);
    }

    /**
     * remove deleted config
     *
     * @param namespace namespace
     * @param configKey configKey
     */
    public void removeDeleted(String namespace, String configKey) {
        Set<String> configs = deletedConfigs.get(namespace);
        if (configs == null) {
            return;
        }
        configs.remove(configKey);
        if (configs.isEmpty()) {
            deletedConfigs.remove(namespace);
        }
    }

    /**
     * get deleted configs data
     */
    public Map<String, Set<String>> deletedConfigs() {
        return Collections.unmodifiableMap(deletedConfigs);
    }
}

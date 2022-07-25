package com.yhzdys.myosotis.data;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * metadata of long polling config (the configuration used in the program)
 */
public final class PollingMetadata {

    private final AtomicLong modifiedVersion = new AtomicLong(1);

    /**
     * medata map of long polling
     * <namespace, PollingData.class>
     */
    private final Map<String, PollingData> pollingDataMap = new ConcurrentHashMap<>(2);

    /**
     * <id, namespace>
     */
    private final Map<Long, String> idNamespaceMap = new ConcurrentHashMap<>(2);

    /**
     * <namespace, <configKey, id>>
     */
    private final Map<String, Map<String, Long>> namespaceKeyIdMap = new ConcurrentHashMap<>(2);

    /**
     * <namespace, <id, configKey>>
     */
    private final Map<String, Map<Long, String>> namespaceIdKeyMap = new ConcurrentHashMap<>(2);

    public void setAll(String namespace, boolean isAll) {
        this.getPollingData(namespace).setAll(isAll);
    }

    public String getConfigKey(Long id, String namespace) {
        return this.getIdKeyMap(namespace).get(id);
    }

    private void updateVersion() {
        modifiedVersion.incrementAndGet();
    }

    public void add(Long id, String namespace, String configKey, Integer version) {
        this.getPollingData(namespace).getData().put(id, version);
        idNamespaceMap.put(id, namespace);
        this.getKeyIdMap(namespace).put(configKey, id);
        this.getIdKeyMap(namespace).put(id, configKey);
        this.updateVersion();
    }

    public void remove(Long id) {
        this.getPollingData(idNamespaceMap.get(id)).getData().remove(id);
        idNamespaceMap.remove(id);
        this.updateVersion();
    }

    /**
     * 不能移除keyIdMap
     */
    public void remove(String namespace, String configKey) {
        Long id = namespaceKeyIdMap.get(namespace).get(configKey);
        if (id != null) {
            this.getPollingData(namespace).getData().remove(id);
            idNamespaceMap.remove(id);
        }
        this.updateVersion();
    }

    public Long getConfigId(String namespace, String configKey) {
        Map<String, Long> keyIdMap = namespaceKeyIdMap.get(namespace);
        if (keyIdMap == null) {
            return null;
        }
        return keyIdMap.get(configKey);
    }

    public Map<String, PollingData> getPollingMap() {
        return Collections.unmodifiableMap(pollingDataMap);
    }

    public long getModifiedVersion() {
        return modifiedVersion.get();
    }

    public void update(MyosotisEvent event) {
        Long id = event.getId();
        int version = event.getVersion();
        String namespace = event.getNamespace() != null ? event.getNamespace() : idNamespaceMap.get(id);
        this.getPollingData(namespace).getData().put(id, version);
        this.updateVersion();
    }

    private PollingData getPollingData(String namespace) {
        return pollingDataMap.computeIfAbsent(
                namespace, cg -> new PollingData(false, namespace, new ConcurrentHashMap<>(2))
        );
    }

    private Map<String, Long> getKeyIdMap(String namespace) {
        return namespaceKeyIdMap.computeIfAbsent(
                namespace, cg -> new ConcurrentHashMap<>(2)
        );
    }

    private Map<Long, String> getIdKeyMap(String namespace) {
        return namespaceIdKeyMap.computeIfAbsent(
                namespace, cg -> new ConcurrentHashMap<>(2)
        );
    }
}

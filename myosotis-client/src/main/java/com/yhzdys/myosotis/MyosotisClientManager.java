package com.yhzdys.myosotis;

import com.yhzdys.myosotis.data.AbsentConfigData;
import com.yhzdys.myosotis.data.CachedConfigData;
import com.yhzdys.myosotis.data.DeletedConfigData;
import com.yhzdys.myosotis.data.PollingConfigData;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.enums.EventType;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.multicast.MyosotisEventMulticaster;
import com.yhzdys.myosotis.executor.PollingScheduler;
import com.yhzdys.myosotis.misc.Converter;
import com.yhzdys.myosotis.misc.LockStore;
import com.yhzdys.myosotis.misc.LoggerFactory;
import com.yhzdys.myosotis.processor.NativeProcessor;
import com.yhzdys.myosotis.processor.Processor;
import com.yhzdys.myosotis.processor.ServerProcessor;
import com.yhzdys.myosotis.processor.SnapshotProcessor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * manage myosotis clients
 * all client shared one scheduled threadPool
 *
 * @see com.yhzdys.myosotis.MyosotisClient
 */
public final class MyosotisClientManager {

    /**
     * clientManager state flag
     */
    private final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * myosotis client holder
     * <namespace, MyosotisClient.class>
     *
     * @see com.yhzdys.myosotis.MyosotisClient
     */
    private final ConcurrentMap<String, MyosotisClient> clientMap = new ConcurrentHashMap<>(2);

    /**
     * cached configs
     */
    private final CachedConfigData cachedConfigData;

    /**
     * cached config metadata
     */
    private final PollingConfigData pollingConfigData;
    private final DeletedConfigData deletedConfigData;
    private final AbsentConfigData absentConfigData;

    /**
     * scheduled threadPool of long polling
     */
    private final PollingScheduler scheduler;

    /**
     * config fetch processor
     */
    private final Processor nativeProcessor;
    private final Processor serverProcessor;
    private final Processor snapshotProcessor;

    private final MyosotisEventMulticaster eventMulticaster;

    public MyosotisClientManager(String serverAddress) {
        this(new MyosotisCustomizer(serverAddress));
    }

    public MyosotisClientManager(MyosotisCustomizer customizer) {
        this.cachedConfigData = new CachedConfigData();
        this.pollingConfigData = new PollingConfigData();
        this.deletedConfigData = new DeletedConfigData();
        this.absentConfigData = new AbsentConfigData();

        this.scheduler = new PollingScheduler();
        if (customizer.isEnableNative()) {
            this.nativeProcessor = new NativeProcessor(cachedConfigData);
        } else {
            this.nativeProcessor = null;
        }
        if (customizer.isEnableSnapshot()) {
            this.snapshotProcessor = new SnapshotProcessor();
        } else {
            this.snapshotProcessor = null;
        }
        this.serverProcessor = new ServerProcessor(customizer, pollingConfigData, absentConfigData);

        this.eventMulticaster = new MyosotisEventMulticaster();
    }

    public MyosotisClient getClient(String namespace) {
        return this.getClient(namespace, null, null);
    }

    public MyosotisClient getClient(String namespace,
                                    NamespaceListener namespaceListener,
                                    List<ConfigListener> configListeners) {
        MyosotisClient client = clientMap.get(namespace);
        if (client != null) {
            return client;
        }
        synchronized (clientMap) {
            client = clientMap.get(namespace);
            if (client != null) {
                return client;
            }
            // init
            if (nativeProcessor != null) {
                nativeProcessor.init(namespace);
            }
            if (snapshotProcessor != null) {
                snapshotProcessor.init(namespace);
            }
            cachedConfigData.add(namespace);
            client = new MyosotisClient(namespace, this);
            clientMap.put(namespace, client);
        }
        if (namespaceListener != null) {
            this.addNamespaceListener(namespaceListener);
        }
        if (CollectionUtils.isNotEmpty(configListeners)) {
            for (ConfigListener configListener : configListeners) {
                if (configListener == null) {
                    continue;
                }
                this.addConfigListener(configListener);
            }
        }
        this.start();
        return client;
    }

    public void addNamespaceListener(NamespaceListener listener) {
        if (listener == null) {
            return;
        }
        String namespace = listener.namespace();
        if (StringUtils.isEmpty(namespace)) {
            LoggerFactory.getLogger().error("Listener's namespace may not be null");
            return;
        }
        MyosotisClient client = clientMap.get(namespace);
        if (client == null) {
            LoggerFactory.getLogger().warn("Add namespaceListener failed, there is no client of namespace: {}", namespace);
            return;
        }
        eventMulticaster.addNamespaceListener(listener);

        pollingConfigData.setAll(namespace, true);

        // to init local cache
        if (haveLocalCache(namespace)) {
            return;
        }
        synchronized (LockStore.get(namespace)) {
            if (haveLocalCache(namespace)) {
                return;
            }
            // init local cache
            this.initConfigs(namespace);
        }
    }

    public void addConfigListener(ConfigListener listener) {
        if (listener == null) {
            return;
        }
        String namespace = listener.namespace();
        String configKey = listener.configKey();
        if (StringUtils.isEmpty(namespace)) {
            LoggerFactory.getLogger().error("Listener's namespace may not be null");
            return;
        }
        if (StringUtils.isEmpty(configKey)) {
            LoggerFactory.getLogger().error("Listener's configKey may not be null");
            return;
        }
        MyosotisClient client = clientMap.get(namespace);
        if (client == null) {
            LoggerFactory.getLogger().warn("Add configListener failed, there is no client of namespace: {}", namespace);
            return;
        }
        eventMulticaster.addConfigListener(listener);

        // automatically add to polling metadata
        String value = this.getConfig(namespace, configKey);
        if (value == null) {
            deletedConfigData.add(0L, namespace, configKey);
        }
    }

    public void start() {
        if (started.get()) {
            // already started
            return;
        }
        if (started.compareAndSet(false, true)) {
            this.doStart();
        }
    }

    /**
     * get config
     * fetch order: local cache > local file > server > snapshot
     */
    String getConfig(String namespace, String configKey) {
        // step.1 get from local cache
        String configValue = cachedConfigData.get(namespace, configKey);
        if (configValue != null) {
            return configValue;
        }
        synchronized (LockStore.get(namespace + ":" + configKey)) {
            configValue = cachedConfigData.get(namespace, configKey);
            if (configValue != null) {
                return configValue;
            }

            // step.2 get from local file
            MyosotisConfig config;
            if (nativeProcessor != null && (config = nativeProcessor.getConfig(namespace, configKey)) != null) {
                cachedConfigData.add(namespace, configKey, config.getConfigValue());
                absentConfigData.remove(namespace, configKey);
                return config.getConfigValue();
            }
            // step.3 get from server
            config = serverProcessor.getConfig(namespace, configKey);
            if (absentConfigData.isAbsent(namespace, configKey)) {
                return null;
            }
            if (config != null) {
                pollingConfigData.add(config.getId(), namespace, configKey, config.getVersion());
                absentConfigData.remove(namespace, configKey);
                if (config.getConfigValue() != null) {
                    cachedConfigData.add(namespace, configKey, config.getConfigValue());
                    if (snapshotProcessor != null) {
                        snapshotProcessor.save(config);
                    }
                    return config.getConfigValue();
                }
            }

            // step.4 get from local snapshot file
            if (snapshotProcessor != null && (config = snapshotProcessor.getConfig(namespace, configKey)) != null) {
                cachedConfigData.add(namespace, configKey, config.getConfigValue());
                pollingConfigData.add(config.getId(), namespace, configKey, config.getVersion());
                return config.getConfigValue();
            }
        }
        return null;
    }

    /**
     * 本地数据不为空 返回true
     */
    private boolean haveLocalCache(String namespace) {
        PollingData pollingData = pollingConfigData.getPollingMap().get(namespace);
        if (pollingData == null) {
            return false;
        }
        return cachedConfigData.containsNamespaceConfig(namespace);
    }

    /**
     * 查询到的配置刷到缓存中，并加入轮询队列
     */
    private void initConfigs(String namespace) {
        List<MyosotisConfig> configs = serverProcessor.getConfigs(namespace);
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        for (MyosotisConfig config : configs) {
            this.initLocalConfig(config);
        }
    }

    private void initLocalConfig(MyosotisConfig config) {
        String namespace = config.getNamespace();
        String configKey = config.getConfigKey();
        absentConfigData.remove(namespace, configKey);

        MyosotisConfig localConfig = nativeProcessor.getConfig(namespace, configKey);
        if (localConfig != null) {
            cachedConfigData.add(namespace, configKey, localConfig.getConfigValue());
            absentConfigData.remove(namespace, configKey);
            return;
        }
        pollingConfigData.add(config.getId(), namespace, configKey, config.getVersion());
        if (config.getConfigValue() != null) {
            cachedConfigData.add(namespace, configKey, config.getConfigValue());
            snapshotProcessor.save(config);
        }
    }

    private void doStart() {
        LoggerFactory.getLogger().info("Myosotis starting...");
        this.scheduler.scheduleAtFixedRate(() -> {
            try {
                this.fetchLocalConfigs();
                this.clearAbsentConfigs();
                this.fetchServerConfigs();
            } catch (Throwable e) {
                LoggerFactory.getLogger().error("Polling config(s) error", e);
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
            }
        }, TimeUnit.SECONDS.toMillis(10), 1, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                this.fetchDeletedConfigs();
            } catch (Throwable e) {
                LoggerFactory.getLogger().error("Fetch deleted config(s) error", e);
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
            }
        }, TimeUnit.SECONDS.toMillis(20), TimeUnit.SECONDS.toMillis(10), TimeUnit.MILLISECONDS);
        LoggerFactory.getLogger().info("Myosotis start completed...");
    }

    private void fetchLocalConfigs() {
        if (nativeProcessor == null) {
            return;
        }
        List<MyosotisEvent> events = nativeProcessor.fetchEvents();
        for (MyosotisEvent event : events) {
            String namespace = event.getNamespace();
            absentConfigData.remove(namespace, event.getConfigKey());
            this.multicastLocalEvent(clientMap.get(namespace), event);
        }
    }

    /**
     * clear absent configs
     */
    private void clearAbsentConfigs() {
        absentConfigData.clear();
    }

    private void fetchServerConfigs() {
        Map<String, PollingData> map = pollingConfigData.getPollingMap();
        if (MapUtils.isEmpty(map)) {
            return;
        }
        List<MyosotisEvent> events = serverProcessor.fetchEvents();
        if (CollectionUtils.isEmpty(events)) {
            return;
        }
        for (MyosotisEvent event : events) {
            MyosotisClient client = clientMap.get(event.getNamespace());
            if (client == null) {
                continue;
            }
            this.multicastServerEvents(client, event);
        }
    }

    /**
     * local config file events
     */
    private void multicastLocalEvent(MyosotisClient client, MyosotisEvent event) {
        String namespace = client.getNamespace();
        String configKey = event.getConfigKey();
        String configValue = event.getConfigValue();
        switch (event.getType()) {
            case ADD:
                Long id = pollingConfigData.getConfigId(namespace, configKey);
                if (id == null) {
                    return;
                }
                pollingConfigData.remove(namespace, configKey);
                event.setId(id).setType(EventType.UPDATE);
                break;
            case UPDATE:
                // not really update
                if (Objects.equals(cachedConfigData.get(namespace, configKey), configValue)) {
                    return;
                }
                cachedConfigData.add(namespace, configKey, configValue);
                break;
            case DELETE:
                MyosotisConfig config = serverProcessor.getConfig(namespace, configKey);
                if (config == null) {
                    id = pollingConfigData.getConfigId(namespace, configKey);
                    if (id == null) {
                        config = snapshotProcessor.getConfig(namespace, configKey);
                        id = config == null ? null : config.getId();
                    }
                    if (id != null && id > 0L) {
                        pollingConfigData.add(id, namespace, configKey, 0);
                    }
                    return;
                }
                pollingConfigData.add(config.getId(), namespace, config.getConfigKey(), config.getVersion());
                if (config.getConfigValue() == null ||
                        Objects.equals(cachedConfigData.get(namespace, configKey), config.getConfigValue())) {
                    return;
                }
                cachedConfigData.add(namespace, configKey, config.getConfigValue());
                event.setConfigValue(config.getConfigValue()).setVersion(config.getVersion()).setType(EventType.UPDATE);
                break;
            default:
                return;
        }
        eventMulticaster.multicastEvent(event);
    }

    /**
     * myosotis server config events
     */
    private void multicastServerEvents(MyosotisClient client, MyosotisEvent event) {
        String namespace = client.getNamespace();
        String configKey = event.getConfigKey();
        String configValue = event.getConfigValue();
        switch (event.getType()) {
            case ADD:
                cachedConfigData.add(namespace, configKey, configValue);
                pollingConfigData.add(event);
                snapshotProcessor.save(Converter.event2Config(event));
                break;
            case UPDATE:
                // not really update
                if (Objects.equals(cachedConfigData.get(namespace, configKey), configValue)) {
                    return;
                }
                cachedConfigData.add(namespace, configKey, configValue);
                pollingConfigData.update(event);
                snapshotProcessor.save(Converter.event2Config(event));
                break;
            case DELETE:
                configKey = pollingConfigData.getConfigKey(event.getId(), namespace);
                event.setConfigKey(configKey);
                cachedConfigData.remove(namespace, configKey);
                pollingConfigData.remove(event.getId());
                if (eventMulticaster.containsConfigListener(namespace, configKey)) {
                    deletedConfigData.add(event.getId(), namespace, configKey);
                }
                break;
            default:
                return;
        }
        eventMulticaster.multicastEvent(event);
    }

    /**
     * 查询被configListener订阅的不存在的配置
     */
    private void fetchDeletedConfigs() {
        Map<String, Map<String, Long>> deletedMap = deletedConfigData.getMap();
        if (MapUtils.isEmpty(deletedMap)) {
            return;
        }

        List<MyosotisConfig> configs = serverProcessor.getConfigs(deletedMap);
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        for (MyosotisConfig config : configs) {
            pollingConfigData.add(config.getId(), config.getNamespace(), config.getConfigKey(), config.getVersion());
            deletedConfigData.remove(config.getNamespace(), config.getConfigKey());
            absentConfigData.remove(config.getNamespace(), config.getConfigKey());
            String configValue = config.getConfigValue();
            if (configValue != null) {
                cachedConfigData.add(config.getNamespace(), config.getConfigKey(), configValue);
                snapshotProcessor.save(config);
            }
            MyosotisEvent event = Converter.config2Event(config, EventType.ADD);
            eventMulticaster.multicastEvent(event);
        }
    }
}

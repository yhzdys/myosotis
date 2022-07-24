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
import com.yhzdys.myosotis.event.publish.EventMulticaster;
import com.yhzdys.myosotis.executor.EventPublishExecutor;
import com.yhzdys.myosotis.executor.LongPollingExecutor;
import com.yhzdys.myosotis.misc.Converter;
import com.yhzdys.myosotis.misc.LockStore;
import com.yhzdys.myosotis.misc.LoggerFactory;
import com.yhzdys.myosotis.processor.LocalProcessor;
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
    private final LongPollingExecutor pollingScheduledPool;

    /**
     * threadPool of event publish
     */
    private final EventPublishExecutor eventPublishSharedPool;
    private final EventMulticaster eventMulticaster;

    /**
     * config fetch processor
     */
    private final Processor localProcessor;
    private final Processor serverProcessor;
    private final Processor snapshotProcessor;

    /**
     * threshold of clear absent config cache (ms.)
     */
    private final long checkAbsentThreshold = TimeUnit.MINUTES.toMillis(1);
    private long lastCheckTimestamp = 0;

    public MyosotisClientManager(String serverAddress) {
        this(new MyosotisCustomizer(serverAddress));
    }

    public MyosotisClientManager(MyosotisCustomizer customizer) {
        this.cachedConfigData = new CachedConfigData();
        this.pollingConfigData = new PollingConfigData();
        this.deletedConfigData = new DeletedConfigData();
        this.absentConfigData = new AbsentConfigData();

        this.pollingScheduledPool = new LongPollingExecutor();
        this.eventPublishSharedPool = new EventPublishExecutor();
        this.eventMulticaster = new EventMulticaster();

        if (customizer.isEnableLocalFile()) {
            this.localProcessor = new LocalProcessor(cachedConfigData);
        } else {
            this.localProcessor = null;
        }
        if (customizer.isEnableSnapshotFile()) {
            this.snapshotProcessor = new SnapshotProcessor();
        } else {
            this.snapshotProcessor = null;
        }
        this.serverProcessor = new ServerProcessor(customizer, pollingConfigData, absentConfigData);
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
            if (localProcessor != null) {
                localProcessor.init(namespace);
            }
            if (snapshotProcessor != null) {
                snapshotProcessor.init(namespace);
            }
            cachedConfigData.add(namespace);
            client = new MyosotisClient(namespace, this, eventPublishSharedPool);
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

    public void addNamespaceListener(NamespaceListener namespaceListener) {
        String namespace = namespaceListener.namespace();
        if (StringUtils.isEmpty(namespace)) {
            LoggerFactory.getLogger().warn("Listener namespace may not be null");
            return;
        }
        MyosotisClient client = clientMap.get(namespace);
        if (client == null) {
            LoggerFactory.getLogger().warn("There is no the client of namespace: {}", namespace);
            return;
        }
        client.setNamespaceListener(namespaceListener);
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

    public void addConfigListener(ConfigListener configListener) {
        String namespace = configListener.namespace();
        if (StringUtils.isEmpty(namespace)) {
            LoggerFactory.getLogger().warn("Listener namespace may not be null");
            return;
        }
        String configKey = configListener.configKey();
        if (StringUtils.isEmpty(configKey)) {
            LoggerFactory.getLogger().warn("Listener configKey may not be null");
            return;
        }

        MyosotisClient client = clientMap.get(namespace);
        if (client == null) {
            LoggerFactory.getLogger().warn("There is no the client of namespace: {}", namespace);
            return;
        }
        client.addConfigListener(configListener);

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
            if (localProcessor != null && (config = localProcessor.getConfig(namespace, configKey)) != null) {
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
        return !cachedConfigData.isEmpty(namespace);
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

        MyosotisConfig localConfig = localProcessor.getConfig(namespace, configKey);
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
        this.pollingScheduledPool.scheduleAtFixedRate(() -> {
            try {
                this.fetchLocalConfigs();
                this.clearAbsentConfigs();
                this.fetchServerConfigs();
            } catch (Throwable e) {
                LoggerFactory.getLogger().error("Polling config(s) error", e);
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
            }
        }, 0, 1, TimeUnit.MILLISECONDS);

        pollingScheduledPool.scheduleAtFixedRate(() -> {
            try {
                this.fetchDeletedConfigs();
            } catch (Throwable e) {
                LoggerFactory.getLogger().error("Fetch deleted config(s) error", e);
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
            }
        }, 1, 10, TimeUnit.SECONDS);
        LoggerFactory.getLogger().info("Myosotis start completed...");
    }

    private void fetchLocalConfigs() {
        if (localProcessor == null) {
            return;
        }
        List<MyosotisEvent> events = localProcessor.fetchEvents();
        for (MyosotisEvent event : events) {
            String namespace = event.getNamespace();
            absentConfigData.remove(namespace, event.getConfigKey());
            this.publishLocalEvent(clientMap.get(namespace), event);
        }
    }

    /**
     * clear absent configs
     */
    private void clearAbsentConfigs() {
        long now = System.currentTimeMillis();
        if ((now - lastCheckTimestamp) < checkAbsentThreshold) {
            return;
        }
        lastCheckTimestamp = now;
        absentConfigData.clear();
    }

    private void fetchServerConfigs() {
        Map<String, PollingData> map = pollingConfigData.getPollingMap();
        if (MapUtils.isEmpty(map)) {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
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
            this.publishServerEvents(client, event);
        }
    }

    /**
     * local config file events
     */
    private void publishLocalEvent(MyosotisClient client, MyosotisEvent event) {
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
        this.triggerListener(client, event);
    }

    /**
     * myosotis server config events
     */
    private void publishServerEvents(MyosotisClient client, MyosotisEvent event) {
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
                if (CollectionUtils.isNotEmpty(clientMap.get(namespace).getConfigListeners(configKey))) {
                    deletedConfigData.add(event.getId(), namespace, configKey);
                }
                break;
            default:
                return;
        }
        this.triggerListener(client, event);
    }

    private void triggerListener(MyosotisClient client, MyosotisEvent event) {
        NamespaceListener namespaceListener = client.getNamespaceListener();
        EventMulticaster.triggerNamespaceListeners(
                client.getNamespaceListenerExecutor(), namespaceListener, event
        );

        List<ConfigListener> configListeners = client.getConfigListeners(event.getConfigKey());
        EventMulticaster.triggerConfigListeners(
                client.getConfigListenerExecutor(), configListeners, event
        );
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
            this.triggerListener(clientMap.get(config.getNamespace()), event);
        }
    }
}

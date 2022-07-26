package com.yhzdys.myosotis;

import com.yhzdys.myosotis.data.AbsentConfigMetadata;
import com.yhzdys.myosotis.data.CachedConfigData;
import com.yhzdys.myosotis.data.DeletedConfigMetadata;
import com.yhzdys.myosotis.data.PollingConfigMetadata;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.enums.EventType;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.multicast.MyosotisEventMulticaster;
import com.yhzdys.myosotis.executor.ScheduledExecutor;
import com.yhzdys.myosotis.misc.Converter;
import com.yhzdys.myosotis.misc.LockStore;
import com.yhzdys.myosotis.misc.LoggerFactory;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * manage myosotis clients
 * all client shared one scheduled threadPool
 *
 * @see com.yhzdys.myosotis.MyosotisClient
 */
public final class MyosotisApplication {

    /**
     * application state flag
     */
    private final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * myosotis client holder
     * <namespace, MyosotisClient.class>
     *
     * @see com.yhzdys.myosotis.MyosotisClient
     */
    private final Map<String, MyosotisClient> clients = new ConcurrentHashMap<>(2);

    /**
     * cached configs
     */
    private final CachedConfigData cachedConfigData;

    /**
     * cached config metadata
     */
    private final PollingConfigMetadata pollingConfigMetadata;
    private final DeletedConfigMetadata deletedConfigMetadata;
    private final AbsentConfigMetadata absentConfigMetadata;

    /**
     * config fetch processor
     */
    private final Processor serverProcessor;
    private final Processor snapshotProcessor;

    private final MyosotisEventMulticaster eventMulticaster;

    public MyosotisApplication(String serverAddress) {
        this(new Config(serverAddress));
    }

    public MyosotisApplication(Config config) {
        this.cachedConfigData = new CachedConfigData();
        this.pollingConfigMetadata = new PollingConfigMetadata();
        this.deletedConfigMetadata = new DeletedConfigMetadata();
        this.absentConfigMetadata = new AbsentConfigMetadata();

        this.serverProcessor = new ServerProcessor(config, pollingConfigMetadata, absentConfigMetadata);
        if (config.isEnableSnapshot()) {
            this.snapshotProcessor = new SnapshotProcessor();
        } else {
            this.snapshotProcessor = null;
        }
        this.eventMulticaster = new MyosotisEventMulticaster();
    }

    public MyosotisClient getClient(String namespace) {
        return this.getClient(namespace, null, null);
    }

    public MyosotisClient getClient(String namespace,
                                    NamespaceListener namespaceListener,
                                    List<ConfigListener> configListeners) {
        MyosotisClient client = clients.get(namespace);
        if (client != null) {
            return client;
        }
        synchronized (clients) {
            client = clients.get(namespace);
            if (client != null) {
                return client;
            }
            if (snapshotProcessor != null) {
                snapshotProcessor.init(namespace);
            }
            cachedConfigData.add(namespace);
            client = new MyosotisClient(namespace, cachedConfigData);
            clients.put(namespace, client);
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
        MyosotisClient client = clients.get(namespace);
        if (client == null) {
            LoggerFactory.getLogger().warn("Add namespaceListener failed, there is no client of namespace: {}", namespace);
            return;
        }
        eventMulticaster.addNamespaceListener(listener);

        pollingConfigMetadata.setAll(namespace, true);

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
        MyosotisClient client = clients.get(namespace);
        if (client == null) {
            LoggerFactory.getLogger().warn("Add configListener failed, there is no client of namespace: {}", namespace);
            return;
        }
        eventMulticaster.addConfigListener(listener);

        // automatically add to polling metadata
        String value = this.getConfig(namespace, configKey);
        if (value == null) {
            deletedConfigMetadata.add(0L, namespace, configKey);
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
     * fetch order: cache > server > snapshot
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
            // step.2 get from server
            MyosotisConfig config = serverProcessor.getConfig(namespace, configKey);
            if (config != null) {
                pollingConfigMetadata.add(config.getId(), namespace, configKey, config.getVersion());
                absentConfigMetadata.remove(namespace, configKey);
                if (config.getConfigValue() != null) {
                    cachedConfigData.add(namespace, configKey, config.getConfigValue());
                    if (snapshotProcessor != null) {
                        snapshotProcessor.save(config);
                    }
                    return config.getConfigValue();
                }
            }
            if (absentConfigMetadata.isAbsent(namespace, configKey)) {
                return null;
            }
            // step.3 get from local snapshot file
            if (snapshotProcessor != null && (config = snapshotProcessor.getConfig(namespace, configKey)) != null) {
                cachedConfigData.add(namespace, configKey, config.getConfigValue());
                pollingConfigMetadata.add(config.getId(), namespace, configKey, config.getVersion());
                return config.getConfigValue();
            }
        }
        return null;
    }

    /**
     * 本地数据不为空 返回true
     */
    private boolean haveLocalCache(String namespace) {
        PollingData pollingData = pollingConfigMetadata.getPollingMap().get(namespace);
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
        absentConfigMetadata.remove(namespace, configKey);
        pollingConfigMetadata.add(config.getId(), namespace, configKey, config.getVersion());
        if (config.getConfigValue() != null) {
            cachedConfigData.add(namespace, configKey, config.getConfigValue());
            snapshotProcessor.save(config);
        }
    }

    private void doStart() {
        LoggerFactory.getLogger().info("Myosotis starting...");
        final ScheduledExecutor executor = new ScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                this.fetchEvents();
                absentConfigMetadata.clear();
            } catch (Throwable e) {
                LoggerFactory.getLogger().error("Polling config(s) error", e);
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
            }
        }, 1000, 1, TimeUnit.MILLISECONDS);

        executor.scheduleAtFixedRate(() -> {
            try {
                this.fetchDeletedConfigs();
            } catch (Throwable e) {
                LoggerFactory.getLogger().error("Fetch deleted config(s) error", e);
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
            }
        }, 2, 10, TimeUnit.SECONDS);
        LoggerFactory.getLogger().info("Myosotis start completed...");
    }

    private void fetchEvents() {
        Map<String, PollingData> map = pollingConfigMetadata.getPollingMap();
        if (MapUtils.isEmpty(map)) {
            return;
        }
        List<MyosotisEvent> events = serverProcessor.fetchEvents();
        if (CollectionUtils.isEmpty(events)) {
            return;
        }
        for (MyosotisEvent event : events) {
            MyosotisClient client = clients.get(event.getNamespace());
            if (client == null) {
                continue;
            }
            this.multicastServerEvents(client, event);
        }
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
                deletedConfigMetadata.remove(namespace, configKey);
                cachedConfigData.add(namespace, configKey, configValue);
                pollingConfigMetadata.add(event.getId(), namespace, configKey, event.getVersion());
                snapshotProcessor.save(Converter.event2Config(event));
                break;
            case UPDATE:
                // not really update
                if (Objects.equals(cachedConfigData.get(namespace, configKey), configValue)) {
                    return;
                }
                cachedConfigData.add(namespace, configKey, configValue);
                pollingConfigMetadata.update(event);
                snapshotProcessor.save(Converter.event2Config(event));
                break;
            case DELETE:
                configKey = pollingConfigMetadata.getConfigKey(event.getId(), namespace);
                event.setConfigKey(configKey);
                cachedConfigData.remove(namespace, configKey);
                pollingConfigMetadata.remove(event.getId());
                if (eventMulticaster.containsConfigListener(namespace, configKey)) {
                    deletedConfigMetadata.add(event.getId(), namespace, configKey);
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
        Map<String, Map<String, Long>> deletedMap = deletedConfigMetadata.getMap();
        if (MapUtils.isEmpty(deletedMap)) {
            return;
        }
        for (String namespace : deletedMap.keySet()) {
            Map<String, Long> configs = deletedMap.get(namespace);
            for (String configKey : configs.keySet()) {
                MyosotisConfig config = serverProcessor.getConfig(namespace, configKey);
                if (config == null) {
                    continue;
                }
                pollingConfigMetadata.add(config.getId(), namespace, configKey, config.getVersion());
                deletedConfigMetadata.remove(namespace, configKey);
                absentConfigMetadata.remove(namespace, configKey);
                String configValue = config.getConfigValue();
                if (configValue != null) {
                    cachedConfigData.add(namespace, configKey, configValue);
                    snapshotProcessor.save(config);
                }
                MyosotisEvent event = Converter.config2Event(config, EventType.ADD);
                eventMulticaster.multicastEvent(event);
            }
        }
    }
}

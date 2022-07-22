package com.yhzdys.myosotis;

import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.enums.EventType;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.publish.MyosotisEventMulticaster;
import com.yhzdys.myosotis.executor.EventPublishExecutor;
import com.yhzdys.myosotis.executor.LongPollingExecutor;
import com.yhzdys.myosotis.metadata.AbsentConfigMetadata;
import com.yhzdys.myosotis.metadata.DeletedConfigMetadata;
import com.yhzdys.myosotis.metadata.PollingConfigMetadata;
import com.yhzdys.myosotis.misc.LockStore;
import com.yhzdys.myosotis.misc.LoggerFactory;
import com.yhzdys.myosotis.processor.LocalProcessor;
import com.yhzdys.myosotis.processor.Processor;
import com.yhzdys.myosotis.processor.ServerProcessor;
import com.yhzdys.myosotis.processor.SnapshotProcessor;
import com.yhzdys.myosotis.processor.UselessProcessor;
import com.yhzdys.myosotis.util.TransUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
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
     * local config cache
     * <namespace, <configKey, configValue>>
     */
    private final ConcurrentMap<String, ConcurrentMap<String, String>> configCacheMap = new ConcurrentHashMap<>(2);

    /**
     * cached config metadata
     */
    private final PollingConfigMetadata pollingConfigMetaData;
    private final DeletedConfigMetadata deletedConfigMetadata;
    private final AbsentConfigMetadata absentConfigMetaData;

    /**
     * scheduled threadPool of long polling
     */
    private final LongPollingExecutor pollingScheduledPool;

    /**
     * threadPool of event publish
     */
    private final EventPublishExecutor eventPublishSharedPool;

    /**
     * config fetch processor
     */
    private final Processor localFileProcessor;
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
        this.pollingConfigMetaData = new PollingConfigMetadata();
        this.deletedConfigMetadata = new DeletedConfigMetadata();
        this.absentConfigMetaData = new AbsentConfigMetadata();

        this.pollingScheduledPool = new LongPollingExecutor();
        this.eventPublishSharedPool = new EventPublishExecutor();

        if (customizer.isEnableLocalFile()) {
            this.localFileProcessor = new LocalProcessor();
        } else {
            this.localFileProcessor = new UselessProcessor();
        }
        this.serverProcessor = new ServerProcessor(customizer, pollingConfigMetaData, absentConfigMetaData);
        if (customizer.isEnableSnapshotFile()) {
            this.snapshotProcessor = new SnapshotProcessor();
        } else {
            this.snapshotProcessor = new UselessProcessor();
        }
    }

    public MyosotisClient getClient(String namespace) {
        return this.getClient(namespace, null, null);
    }

    public MyosotisClient getClient(final String namespace,
                                    final NamespaceListener namespaceListener,
                                    final List<ConfigListener> configListeners) {
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
            localFileProcessor.init(namespace);
            snapshotProcessor.init(namespace);
            configCacheMap.put(namespace, new ConcurrentHashMap<>(2));

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
        pollingConfigMetaData.setAll(namespace, true);

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
     * fetch order: local cache > local file > server > snapshot
     */
    String getConfig(String namespace, String configKey) {
        // step.1 get from local cache
        Map<String, String> configs = configCacheMap.get(namespace);
        String configValue = configs.get(configKey);
        if (configValue != null) {
            return configValue;
        }
        synchronized (LockStore.get(namespace + ":" + configKey)) {
            configValue = configs.get(configKey);
            if (configValue != null) {
                return configValue;
            }

            // step.2 get from local file
            MyosotisConfig config = localFileProcessor.getConfig(namespace, configKey);
            if (config != null) {
                configs.put(configKey, config.getConfigValue());
                absentConfigMetaData.remove(namespace, configKey);
                return config.getConfigValue();
            }
            if (absentConfigMetaData.isAbsent(namespace, configKey)) {
                return null;
            }

            // step.3 get from server
            config = serverProcessor.getConfig(namespace, configKey);
            // 服务端获取,返回空数据,有两种情况,这里需要单独区分开来 1.和服务端通信失败 2.配置在服务端不存在.
            if (absentConfigMetaData.isAbsent(namespace, configKey)) {
                return null;
            }
            if (config != null) {
                pollingConfigMetaData.add(config.getId(), namespace, configKey, config.getVersion());
                absentConfigMetaData.remove(namespace, configKey);
                if (config.getConfigValue() != null) {
                    configs.put(configKey, config.getConfigValue());
                    snapshotProcessor.save(config);
                    return config.getConfigValue();
                }
            }

            // step.4 get from local snapshot file
            try {
                // 从服务端获取仍然为空(服务端重启、宕机等异常情况)尝试从本地snapshot文件加载配置
                config = snapshotProcessor.getConfig(namespace, configKey);
                if (config != null) {
                    configs.put(configKey, config.getConfigValue());
                    pollingConfigMetaData.add(config.getId(), namespace, configKey, config.getVersion());
                    return configs.get(configKey);
                }
            } catch (Throwable e) {
                LoggerFactory.getLogger().error("Get config from local snapshot file failed", e);
            }
        }
        return null;
    }

    /**
     * 本地数据不为空 返回true
     */
    private boolean haveLocalCache(String namespace) {
        PollingData pollingData = pollingConfigMetaData.getPollingMap().get(namespace);
        if (pollingData == null) {
            return false;
        }
        return MapUtils.isNotEmpty(configCacheMap.get(namespace));
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
        absentConfigMetaData.remove(namespace, configKey);

        MyosotisConfig localConfig = localFileProcessor.getConfig(namespace, configKey);
        if (localConfig != null) {
            configCacheMap.get(namespace).put(configKey, localConfig.getConfigValue());
            absentConfigMetaData.remove(namespace, configKey);
            return;
        }
        pollingConfigMetaData.add(config.getId(), namespace, configKey, config.getVersion());
        if (config.getConfigValue() != null) {
            configCacheMap.get(namespace).put(configKey, config.getConfigValue());
            snapshotProcessor.save(config);
        }
    }

    private void doStart() {
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
    }

    private void fetchLocalConfigs() {
        if (localFileProcessor instanceof UselessProcessor) {
            return;
        }
        Collection<MyosotisClient> clients = clientMap.values();
        for (MyosotisClient client : clients) {
            String namespace = client.getNamespace();
            List<MyosotisEvent> events = localFileProcessor.fetchEvents(configCacheMap.get(namespace), namespace);
            for (MyosotisEvent event : events) {
                absentConfigMetaData.remove(namespace, event.getConfigKey());
                this.publishLocalEvent(client, event);
            }
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
        absentConfigMetaData.clear();
    }

    private void fetchServerConfigs() {
        Map<String, PollingData> map = pollingConfigMetaData.getPollingMap();
        if (MapUtils.isEmpty(map)) {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            return;
        }
        List<MyosotisEvent> events = serverProcessor.pollingEvents();
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
    private void publishLocalEvent(final MyosotisClient client, MyosotisEvent event) {
        String namespace = client.getNamespace();
        String configKey = event.getConfigKey();

        ConcurrentMap<String, String> configs = configCacheMap.get(namespace);
        switch (event.getType()) {
            case ADD:
                Long id = pollingConfigMetaData.getConfigId(namespace, configKey);
                if (id == null) {
                    return;
                }
                pollingConfigMetaData.remove(namespace, configKey);
                event.setId(id).setType(EventType.UPDATE);
                break;
            case UPDATE:
                if (Objects.equals(configs.get(configKey), event.getConfigValue())) {
                    return;
                }
                configs.put(configKey, event.getConfigValue());
                break;
            case DELETE:
                MyosotisConfig config = serverProcessor.getConfig(namespace, configKey);
                if (config == null) {
                    id = pollingConfigMetaData.getConfigId(namespace, configKey);
                    if (id == null) {
                        config = snapshotProcessor.getConfig(namespace, configKey);
                        id = config == null ? null : config.getId();
                    }
                    if (id != null && id > 0L) {
                        pollingConfigMetaData.add(id, namespace, configKey, 0);
                    }
                    return;
                }
                pollingConfigMetaData.add(config.getId(), namespace, config.getConfigKey(), config.getVersion());
                if (config.getConfigValue() == null || Objects.equals(config.getConfigValue(), configs.get(configKey))) {
                    return;
                }
                configs.put(configKey, config.getConfigValue());
                event.setConfigValue(config.getConfigValue()).setVersion(config.getVersion()).setType(EventType.UPDATE);
                break;
            default:
                return;
        }
        triggerListener(client, event);
    }

    /**
     * myosotis server config events
     */
    private void publishServerEvents(final MyosotisClient client, final MyosotisEvent event) {
        boolean needPublish = false;
        MyosotisConfig config = TransUtil.event2Config(event);

        ConcurrentMap<String, String> configs = configCacheMap.get(client.getNamespace());
        switch (event.getType()) {
            case ADD:
                configs.put(event.getConfigKey(), event.getConfigValue());
                pollingConfigMetaData.add(event);
                snapshotProcessor.save(config);
                needPublish = true;
                break;
            case UPDATE:
                String configValue = configs.get(event.getConfigKey());
                if (!Objects.equals(configValue, event.getConfigValue())) {
                    configs.put(event.getConfigKey(), event.getConfigValue());
                    needPublish = true;
                }
                pollingConfigMetaData.update(event);
                snapshotProcessor.save(config);
                break;
            case DELETE:
                String namespace = event.getNamespace();
                String configKey = pollingConfigMetaData.getConfigKey(event.getId(), namespace);
                configs.remove(configKey);
                event.setConfigKey(configKey);
                pollingConfigMetaData.remove(event.getId());
                if (CollectionUtils.isNotEmpty(clientMap.get(namespace).getConfigListeners(configKey))) {
                    deletedConfigMetadata.add(event.getId(), namespace, configKey);
                }
                needPublish = true;
                break;
            default:
                return;
        }
        if (needPublish) {
            this.triggerListener(client, event);
        }
    }

    private void triggerListener(final MyosotisClient client, final MyosotisEvent event) {
        NamespaceListener namespaceListener = client.getNamespaceListener();
        MyosotisEventMulticaster.triggerNamespaceListeners(
                client.getNamespaceListenerExecutorFactory(), namespaceListener, event
        );

        List<ConfigListener> configListeners = client.getConfigListeners(event.getConfigKey());
        MyosotisEventMulticaster.triggerConfigListeners(
                client.getConfigListenerExecutorFactory(), configListeners, event
        );
    }

    /**
     * 查询被configListener订阅的不存在的配置
     */
    private void fetchDeletedConfigs() {
        Map<String, Map<String, Long>> deletedMap = deletedConfigMetadata.getMap();
        if (MapUtils.isEmpty(deletedMap)) {
            return;
        }

        List<MyosotisConfig> configs = serverProcessor.getConfigs(deletedMap);
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        for (MyosotisConfig config : configs) {
            pollingConfigMetaData.add(config.getId(), config.getNamespace(), config.getConfigKey(), config.getVersion());
            deletedConfigMetadata.remove(config.getNamespace(), config.getConfigKey());
            absentConfigMetaData.remove(config.getNamespace(), config.getConfigKey());
            String configValue = config.getConfigValue();
            if (configValue != null) {
                configCacheMap.get(config.getNamespace()).put(config.getConfigKey(), configValue);
                snapshotProcessor.save(config);
            }
            MyosotisEvent event = TransUtil.config2Event(config, EventType.ADD);
            this.triggerListener(clientMap.get(config.getNamespace()), event);
        }
    }
}

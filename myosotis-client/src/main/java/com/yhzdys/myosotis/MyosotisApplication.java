package com.yhzdys.myosotis;

import com.yhzdys.myosotis.data.CachedConfig;
import com.yhzdys.myosotis.data.ConfigMetadata;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.enums.EventType;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.multicast.EventMulticaster;
import com.yhzdys.myosotis.executor.PollingExecutor;
import com.yhzdys.myosotis.misc.LoggerFactory;
import com.yhzdys.myosotis.processor.Processor;
import com.yhzdys.myosotis.processor.ServerProcessor;
import com.yhzdys.myosotis.processor.SnapshotProcessor;
import com.yhzdys.myosotis.support.LockStore;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public final class MyosotisApplication {

    /**
     * <namespace, MyosotisClient.class>
     */
    private final Map<String, MyosotisClient> clients = new ConcurrentHashMap<>(2);

    /**
     * cached config(s)
     */
    private final CachedConfig cachedConfig;

    /**
     * metadata of cached config(s)
     */
    private final ConfigMetadata configMetadata;

    /**
     * config(s) processor
     */
    private final Processor serverProcessor;
    private final Processor snapshotProcessor;

    private final EventMulticaster multicaster;

    public MyosotisApplication(String serverAddress) {
        this(new Config(serverAddress));
    }

    public MyosotisApplication(Config config) {
        this.configMetadata = new ConfigMetadata();
        this.cachedConfig = new CachedConfig();

        this.serverProcessor = new ServerProcessor(config, configMetadata);
        this.snapshotProcessor = new SnapshotProcessor(config.isEnableSnapshot());
        this.multicaster = new EventMulticaster();

        this.start();
    }

    public MyosotisClient getClient(String namespace) {
        MyosotisClient client = clients.get(namespace);
        if (client != null) {
            return client;
        }
        synchronized (clients) {
            client = clients.get(namespace);
            if (client != null) {
                return client;
            }
            snapshotProcessor.init(namespace);
            cachedConfig.add(namespace);
            client = new MyosotisClient(this, namespace);
            clients.put(namespace, client);
        }
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
        // init client if absent
        this.getClient(namespace);
        if (configMetadata.isPollingAll(namespace)) {
            return;
        }
        synchronized (LockStore.get(namespace)) {
            if (configMetadata.isPollingAll(namespace)) {
                return;
            }
            // init namespace configs
            this.initNamespace(namespace);
            configMetadata.setPollingAll(namespace);
        }
        multicaster.addNamespaceListener(listener);
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
        // init client if absent
        this.getClient(namespace);
        // init polling metadata
        String value = this.getConfig(namespace, configKey);
        if (value == null) {
            configMetadata.addPolling(namespace, configKey, 0);
        }
        multicaster.addConfigListener(listener);
    }

    public Collection<MyosotisClient> clients() {
        return Collections.unmodifiableCollection(clients.values());
    }

    /**
     * get config
     * fetch order: cache > server > snapshot
     */
    String getConfig(String namespace, String configKey) {
        // step.1 get from local cache
        String configValue = cachedConfig.get(namespace, configKey);
        if (configValue != null) {
            return configValue;
        }
        synchronized (LockStore.get(namespace + ":" + configKey)) {
            configValue = cachedConfig.get(namespace, configKey);
            if (configValue != null) {
                return configValue;
            }
            // step.2 get from server
            MyosotisConfig config = serverProcessor.getConfig(namespace, configKey);
            if (config != null) {
                configMetadata.addPolling(namespace, configKey, config.getVersion());
                configMetadata.removeAbsent(namespace, configKey);
                if (config.getConfigValue() != null) {
                    cachedConfig.add(namespace, configKey, config.getConfigValue());
                    snapshotProcessor.save(config);
                    return config.getConfigValue();
                }
            }
            if (configMetadata.isAbsent(namespace, configKey)) {
                return null;
            }
            // step.3 get from snapshot file
            config = snapshotProcessor.getConfig(namespace, configKey);
            if (config != null) {
                cachedConfig.add(namespace, configKey, config.getConfigValue());
                configMetadata.addPolling(namespace, configKey, config.getVersion());
                return config.getConfigValue();
            }
        }
        return null;
    }

    private void initNamespace(String namespace) {
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
        configMetadata.removeAbsent(namespace, configKey);
        configMetadata.addPolling(namespace, configKey, config.getVersion());
        if (config.getConfigValue() != null) {
            cachedConfig.add(namespace, configKey, config.getConfigValue());
            snapshotProcessor.save(config);
        }
    }

    private void start() {
        LoggerFactory.getLogger().info("Myosotis starting...");
        final PollingExecutor executor = new PollingExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                this.fetchEvents();
                configMetadata.clearAbsent();
            } catch (Throwable e) {
                LoggerFactory.getLogger().error("Polling config(s) error", e);
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
        LoggerFactory.getLogger().info("Myosotis start completed...");
    }

    private void fetchEvents() {
        Collection<PollingData> pollingData = configMetadata.pollingData();
        if (CollectionUtils.isEmpty(pollingData)) {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
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
            this.multicastEvents(client, event);
        }
    }

    private void multicastEvents(MyosotisClient client, MyosotisEvent event) {
        String namespace = client.getNamespace();
        String configKey = event.getConfigKey();
        String configValue = event.getConfigValue();

        if (EventType.UPDATE.equals(event.getType())) {
            configMetadata.addPolling(namespace, configKey, event.getVersion());
            // not really update
            if (Objects.equals(configValue, cachedConfig.get(namespace, configKey))) {
                return;
            }
            cachedConfig.add(namespace, configKey, configValue);
            snapshotProcessor.save(this.event2Config(event));
        } else {
            switch (event.getType()) {
                case ADD:
                    configMetadata.addPolling(namespace, configKey, event.getVersion());
                    cachedConfig.add(namespace, configKey, configValue);
                    snapshotProcessor.save(this.event2Config(event));
                    configMetadata.removeAbsent(namespace, configKey);
                    break;
                case DELETE:
                    if (multicaster.containsListener(namespace, configKey)) {
                        // reset polling version
                        configMetadata.addPolling(namespace, configKey, 0);
                    } else {
                        configMetadata.removePolling(namespace, configKey);
                    }
                    cachedConfig.remove(namespace, configKey);
                    configMetadata.addAbsent(namespace, configKey);
                    break;
                default:
                    return;
            }
        }
        multicaster.multicast(event);
    }

    private MyosotisConfig event2Config(MyosotisEvent event) {
        MyosotisConfig config = new MyosotisConfig();
        config.setId(event.getId());
        config.setNamespace(event.getNamespace());
        config.setConfigKey(event.getConfigKey());
        config.setConfigValue(event.getConfigValue());
        config.setVersion(event.getVersion());
        return config;
    }
}

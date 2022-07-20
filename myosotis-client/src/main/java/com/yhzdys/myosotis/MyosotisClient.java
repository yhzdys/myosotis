package com.yhzdys.myosotis;

import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.publish.executor.ConfigListenerExecutorFactory;
import com.yhzdys.myosotis.event.publish.executor.NamespaceListenerExecutorFactory;
import com.yhzdys.myosotis.misc.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * facade of myosotis configs
 */
public final class MyosotisClient {

    private final String namespace;

    private final MyosotisClientManager clientManager;

    private final ConfigListenerExecutorFactory configListenerExecutorFactory;

    private final NamespaceListenerExecutorFactory namespaceListenerExecutorFactory;
    /**
     * <configKey, ConfigListener.class>
     */
    private final ConcurrentMap<String, CopyOnWriteArrayList<ConfigListener>> configListenersMap = new ConcurrentHashMap<>(0);
    private NamespaceListener namespaceListener;

    MyosotisClient(final String namespace,
                   final MyosotisClientManager clientManager,
                   final ThreadPoolExecutor sharedPool) {
        this.namespace = namespace;
        this.clientManager = clientManager;
        this.configListenerExecutorFactory = new ConfigListenerExecutorFactory(sharedPool);
        this.namespaceListenerExecutorFactory = new NamespaceListenerExecutorFactory(sharedPool);
    }

    public String getConfig(final String configKey) {
        return clientManager.getConfig(namespace, configKey);
    }

    public String getConfig(final String configKey, final String defaultValue) {
        String configValue = clientManager.getConfig(namespace, configKey);
        return configValue == null ? defaultValue : configValue;
    }

    void addConfigListener(ConfigListener configListener) {
        String configKey = configListener.configKey();
        CopyOnWriteArrayList<ConfigListener> configListeners = configListenersMap.computeIfAbsent(
                configKey, ck -> new CopyOnWriteArrayList<>()
        );
        configListeners.add(configListener);
    }

    public String getNamespace() {
        return namespace;
    }

    NamespaceListener getNamespaceListener() {
        return namespaceListener;
    }

    void setNamespaceListener(final NamespaceListener namespaceListener) {
        if (this.namespaceListener != null) {
            LoggerFactory.getLogger().error("Listener of namespace: {} already exists", namespace);
            return;
        }
        synchronized (this) {
            if (this.namespaceListener != null) {
                LoggerFactory.getLogger().error("Listener of namespace: {} already exists", namespace);
                return;
            }
            this.namespaceListener = namespaceListener;
        }
    }

    List<ConfigListener> getConfigListeners(String configKey) {
        return configListenersMap.get(configKey);
    }

    ConfigListenerExecutorFactory getConfigListenerExecutorFactory() {
        return configListenerExecutorFactory;
    }

    NamespaceListenerExecutorFactory getNamespaceListenerExecutorFactory() {
        return namespaceListenerExecutorFactory;
    }
}

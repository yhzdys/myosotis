package com.yhzdys.myosotis;

import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.publish.executor.ConfigListenerExecutor;
import com.yhzdys.myosotis.event.publish.executor.NamespaceListenerExecutor;
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

    private final ConfigListenerExecutor configListenerExecutor;

    private final NamespaceListenerExecutor namespaceListenerExecutor;
    /**
     * <configKey, ConfigListener.class>
     */
    private final ConcurrentMap<String, List<ConfigListener>> configListenersMap = new ConcurrentHashMap<>(0);
    private NamespaceListener namespaceListener;

    MyosotisClient(String namespace,
                   MyosotisClientManager clientManager,
                   ThreadPoolExecutor sharedPool) {
        this.namespace = namespace;
        this.clientManager = clientManager;
        this.configListenerExecutor = new ConfigListenerExecutor(sharedPool);
        this.namespaceListenerExecutor = new NamespaceListenerExecutor(sharedPool);
    }

    public String getConfig(String configKey) {
        return clientManager.getConfig(namespace, configKey);
    }

    public String getConfig(String configKey, String defaultValue) {
        String configValue = clientManager.getConfig(namespace, configKey);
        return configValue == null ? defaultValue : configValue;
    }

    void addConfigListener(ConfigListener configListener) {
        String configKey = configListener.configKey();
        List<ConfigListener> configListeners = configListenersMap.computeIfAbsent(
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

    void setNamespaceListener(NamespaceListener namespaceListener) {
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

    ConfigListenerExecutor getConfigListenerExecutor() {
        return configListenerExecutor;
    }

    NamespaceListenerExecutor getNamespaceListenerExecutor() {
        return namespaceListenerExecutor;
    }
}

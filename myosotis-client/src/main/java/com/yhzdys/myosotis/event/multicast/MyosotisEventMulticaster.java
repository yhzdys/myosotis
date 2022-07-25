package com.yhzdys.myosotis.event.multicast;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.multicast.executor.ConfigListenerExecutor;
import com.yhzdys.myosotis.event.multicast.executor.NamespaceListenerExecutor;
import com.yhzdys.myosotis.executor.EventMulticasterExecutor;
import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.misc.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * multicast config change event
 *
 * @see com.yhzdys.myosotis.event.listener.Listener
 * @see com.yhzdys.myosotis.event.listener.NamespaceListener
 * @see com.yhzdys.myosotis.event.listener.ConfigListener
 */
public final class MyosotisEventMulticaster {

    /**
     * threadPool for multicast event
     */
    private final EventMulticasterExecutor sharedPool = new EventMulticasterExecutor();

    private final Map<String, NamespaceListenerExecutor> namespaceExecutors = new ConcurrentHashMap<>(0);
    private final Map<String, NamespaceListener> namespaceListeners = new ConcurrentHashMap<>(0);

    private final Map<String, ConfigListenerExecutor> configExecutors = new ConcurrentHashMap<>(0);
    private final Map<String, Map<String, List<ConfigListener>>> configListeners = new ConcurrentHashMap<>(0);

    private static void triggerNamespaceListeners(NamespaceListener listener, MyosotisEvent event) {
        try {
            listener.handle(event);
        } catch (Throwable e) {
            LoggerFactory.getLogger().error("Trigger namespaceListener error, event: {}", JsonUtil.toString(event), e);
        }
    }

    private static void triggerConfigListener(ConfigListener listener, MyosotisEvent event) {
        try {
            listener.handle(event);
        } catch (Throwable e) {
            LoggerFactory.getLogger().error("Trigger configListener error, event: {}", JsonUtil.toString(event), e);
        }
    }

    public void addNamespaceListener(NamespaceListener listener) {
        String namespace = listener.namespace();
        namespaceExecutors.computeIfAbsent(namespace, k -> new NamespaceListenerExecutor(sharedPool));
        namespaceListeners.putIfAbsent(namespace, listener);
    }

    public void addConfigListener(ConfigListener listener) {
        String namespace = listener.namespace();
        String configKey = listener.configKey();
        configExecutors.computeIfAbsent(namespace, k -> new ConfigListenerExecutor(sharedPool));
        configListeners.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>(2))
                .computeIfAbsent(configKey, k -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    public boolean containsConfigListener(String namespace, String configKey) {
        Map<String, List<ConfigListener>> listenerMap = configListeners.get(namespace);
        if (listenerMap == null) {
            return false;
        }
        List<ConfigListener> listeners = listenerMap.get(configKey);
        return listeners != null;
    }

    public void multicastEvent(MyosotisEvent event) {
        this.triggerNamespaceListeners(event);
        this.triggerConfigListeners(event);
    }

    /**
     * trigger namespaceListener
     */
    private void triggerNamespaceListeners(MyosotisEvent event) {
        String namespace = event.getNamespace();
        NamespaceListenerExecutor executor = namespaceExecutors.get(namespace);
        if (executor == null) {
            return;
        }
        NamespaceListener namespaceListener = namespaceListeners.get(namespace);
        executor.execute(
                new NamespaceListenerExecutor.Task(
                        event.getConfigKey(), () -> triggerNamespaceListeners(namespaceListener, event)
                )
        );
    }

    /**
     * trigger configListeners
     */
    private void triggerConfigListeners(MyosotisEvent event) {
        ConfigListenerExecutor executor = configExecutors.get(event.getNamespace());
        if (executor == null) {
            return;
        }
        List<ConfigListener> listeners = configListeners.get(event.getNamespace()).get(event.getConfigKey());
        for (ConfigListener listener : listeners) {
            executor.getExecutor(listener)
                    .execute(
                            () -> triggerConfigListener(listener, event)
                    );
        }
    }
}
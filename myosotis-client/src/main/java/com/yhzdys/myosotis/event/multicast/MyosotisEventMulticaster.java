package com.yhzdys.myosotis.event.multicast;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.Listener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.multicast.executor.ConfigExecutor;
import com.yhzdys.myosotis.event.multicast.executor.EventCommand;
import com.yhzdys.myosotis.event.multicast.executor.Executor;
import com.yhzdys.myosotis.event.multicast.executor.NamespaceExecutor;
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

    private final Map<String, ListenerWrapper> namespaceListeners = new ConcurrentHashMap<>(0);

    private final Map<String, Map<String, List<ListenerWrapper>>> configListeners = new ConcurrentHashMap<>(0);

    private static void triggerNamespaceListener(NamespaceListener listener, MyosotisEvent event) {
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
        namespaceListeners.computeIfAbsent(namespace, k -> new ListenerWrapper(listener, new NamespaceExecutor(sharedPool)));
    }

    public void addConfigListener(ConfigListener listener) {
        String namespace = listener.namespace();
        String configKey = listener.configKey();
        configListeners.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>(2))
                .computeIfAbsent(configKey, k -> new CopyOnWriteArrayList<>())
                .add(new ListenerWrapper(listener, new ConfigExecutor(sharedPool)));
    }

    public boolean containsConfigListener(String namespace, String configKey) {
        Map<String, List<ListenerWrapper>> listenerMap = configListeners.get(namespace);
        if (listenerMap == null) {
            return false;
        }
        List<ListenerWrapper> listeners = listenerMap.get(configKey);
        return listeners != null;
    }

    public void multicastEvent(MyosotisEvent event) {
        this.triggerNamespaceListener(event);
        this.triggerConfigListeners(event);
    }

    /**
     * trigger namespaceListener
     */
    private void triggerNamespaceListener(MyosotisEvent event) {
        String namespace = event.getNamespace();
        ListenerWrapper listenerWrapper = namespaceListeners.get(namespace);
        if (listenerWrapper == null) {
            return;
        }
        NamespaceListener listener = (NamespaceListener) listenerWrapper.getListener();
        listenerWrapper.getExecutor().execute(
                new EventCommand(event.getConfigKey(), () -> triggerNamespaceListener(listener, event))
        );
    }

    /**
     * trigger configListener(s)
     */
    private void triggerConfigListeners(MyosotisEvent event) {
        Map<String, List<ListenerWrapper>> listenerMap = configListeners.get(event.getNamespace());
        if (listenerMap == null) {
            return;
        }
        List<ListenerWrapper> listeners = listenerMap.get(event.getConfigKey());
        for (ListenerWrapper listenerWrapper : listeners) {
            ConfigListener listener = (ConfigListener) listenerWrapper.getListener();
            listenerWrapper.getExecutor().execute(
                    new EventCommand(null, () -> triggerConfigListener(listener, event))
            );
        }
    }

    private static final class ListenerWrapper {
        private final Listener listener;
        private final Executor executor;

        public ListenerWrapper(Listener listener, Executor executor) {
            this.listener = listener;
            this.executor = executor;
        }

        public Listener getListener() {
            return listener;
        }

        public Executor getExecutor() {
            return executor;
        }
    }
}
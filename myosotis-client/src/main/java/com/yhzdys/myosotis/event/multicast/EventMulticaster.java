package com.yhzdys.myosotis.event.multicast;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.Listener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.multicast.actuator.Actuator;
import com.yhzdys.myosotis.event.multicast.actuator.ConfigEventActuator;
import com.yhzdys.myosotis.event.multicast.actuator.EventCommand;
import com.yhzdys.myosotis.event.multicast.actuator.NamespaceEventActuator;
import com.yhzdys.myosotis.executor.EventMulticasterExecutor;
import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.misc.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

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
public final class EventMulticaster {

    /**
     * threadPool for multicast event
     */
    private final EventMulticasterExecutor executor = new EventMulticasterExecutor();

    /**
     * namespaceListener holder
     * <namespace, ListenerWrapper>
     */
    private final Map<String, ListenerWrapper> namespaceListeners = new ConcurrentHashMap<>(0);

    /**
     * configListener holder
     * <namespace, <configKey, List<ListenerWrapper>>>
     */
    private final Map<String, Map<String, List<ListenerWrapper>>> configListeners = new ConcurrentHashMap<>(0);

    private static void triggerListener(Listener listener, MyosotisEvent event) {
        try {
            listener.handle(event);
        } catch (Throwable e) {
            LoggerFactory.getLogger().error("Trigger Listener error, event: {}", JsonUtil.toString(event), e);
        }
    }

    /**
     * add namespaceListener
     *
     * @param listener namespaceListener
     */
    public void addNamespaceListener(NamespaceListener listener) {
        String namespace = listener.namespace();
        namespaceListeners.computeIfAbsent(
                namespace, k -> new ListenerWrapper(listener, new NamespaceEventActuator(executor))
        );
    }

    /**
     * add configListener
     *
     * @param listener configListener
     */
    public void addConfigListener(ConfigListener listener) {
        String namespace = listener.namespace();
        String configKey = listener.configKey();
        configListeners.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>(2))
                .computeIfAbsent(configKey, k -> new CopyOnWriteArrayList<>())
                .add(new ListenerWrapper(listener, new ConfigEventActuator(executor)));
    }

    /**
     * contains configListener
     *
     * @param namespace namespace
     * @param configKey configKey
     * @return boolean
     */
    public boolean containsListener(String namespace, String configKey) {
        Map<String, List<ListenerWrapper>> listenerMap = configListeners.get(namespace);
        if (listenerMap == null) {
            return false;
        }
        List<ListenerWrapper> listeners = listenerMap.get(configKey);
        return listeners != null;
    }

    /**
     * multicast event
     *
     * @param event event
     */
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
        listenerWrapper.getActuator().actuate(
                new EventCommand(event.getConfigKey(), () -> triggerListener(listener, event))
        );
    }

    /**
     * trigger configListener(s)
     */
    private void triggerConfigListeners(MyosotisEvent event) {
        Map<String, List<ListenerWrapper>> listenerMap = configListeners.get(event.getNamespace());
        if (MapUtils.isEmpty(listenerMap)) {
            return;
        }
        List<ListenerWrapper> listeners = listenerMap.get(event.getConfigKey());
        if (CollectionUtils.isEmpty(listeners)) {
            return;
        }
        for (ListenerWrapper listenerWrapper : listeners) {
            ConfigListener listener = (ConfigListener) listenerWrapper.getListener();
            listenerWrapper.getActuator().actuate(
                    new EventCommand(() -> triggerListener(listener, event))
            );
        }
    }

    private static final class ListenerWrapper {
        private final Listener listener;
        private final Actuator actuator;

        public ListenerWrapper(Listener listener, Actuator actuator) {
            this.listener = listener;
            this.actuator = actuator;
        }

        public Listener getListener() {
            return listener;
        }

        public Actuator getActuator() {
            return actuator;
        }
    }
}
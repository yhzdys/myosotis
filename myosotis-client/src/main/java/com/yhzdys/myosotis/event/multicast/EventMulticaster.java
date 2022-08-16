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
import com.yhzdys.myosotis.misc.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventMulticaster {

    private final EventMulticasterExecutor executor = new EventMulticasterExecutor();

    /**
     * <namespace, ListenerWrapper>
     */
    private final Map<String, ActuatorWrapper> namespaceListeners = new ConcurrentHashMap<>(0);

    /**
     * <namespace, <configKey, List<ListenerWrapper>>>
     */
    private final Map<String, Map<String, List<ActuatorWrapper>>> configListeners = new ConcurrentHashMap<>(0);

    public boolean containsListener(String namespace, String configKey) {
        Map<String, List<ActuatorWrapper>> listenerMap = configListeners.get(namespace);
        if (MapUtils.isEmpty(listenerMap)) {
            return false;
        }
        List<ActuatorWrapper> listeners = listenerMap.get(configKey);
        return CollectionUtils.isNotEmpty(listeners);
    }

    public void addNamespaceListener(NamespaceListener listener) {
        String namespace = listener.namespace();
        namespaceListeners.computeIfAbsent(
                namespace, k -> new ActuatorWrapper(listener, new NamespaceEventActuator(executor))
        );
    }

    public void addConfigListener(ConfigListener listener) {
        String namespace = listener.namespace();
        String configKey = listener.configKey();
        configListeners.computeIfAbsent(namespace, k -> new ConcurrentHashMap<>(2))
                .computeIfAbsent(configKey, k -> new CopyOnWriteArrayList<>())
                .add(new ActuatorWrapper(listener, new ConfigEventActuator(executor)));
    }

    public void multicast(MyosotisEvent event) {
        this.triggerNamespaceListener(event);
        this.triggerConfigListeners(event);
    }

    private void triggerNamespaceListener(MyosotisEvent event) {
        ActuatorWrapper actuator = namespaceListeners.get(event.getNamespace());
        if (actuator == null) {
            return;
        }
        Listener listener = actuator.getListener();
        actuator.actuate(
                new EventCommand(event.getConfigKey(), () -> this.triggerListener(listener, event))
        );
    }

    private void triggerConfigListeners(MyosotisEvent event) {
        Map<String, List<ActuatorWrapper>> listenerMap = configListeners.get(event.getNamespace());
        if (MapUtils.isEmpty(listenerMap)) {
            return;
        }
        List<ActuatorWrapper> listeners = listenerMap.get(event.getConfigKey());
        if (CollectionUtils.isEmpty(listeners)) {
            return;
        }
        for (ActuatorWrapper actuator : listeners) {
            Listener listener = actuator.getListener();
            actuator.actuate(
                    new EventCommand(() -> this.triggerListener(listener, event))
            );
        }
    }

    private void triggerListener(Listener listener, MyosotisEvent event) {
        try {
            listener.handle(event);
        } catch (Throwable e) {
            LoggerFactory.getLogger().error("Trigger Listener({}) failed, msg: {}", listener.getClass().getName(), e.getMessage());
        }
    }

    private static final class ActuatorWrapper implements Actuator {
        private final Listener listener;
        private final Actuator actuator;

        private ActuatorWrapper(Listener listener, Actuator actuator) {
            this.listener = listener;
            this.actuator = actuator;
        }

        private Listener getListener() {
            return listener;
        }

        @Override
        public void actuate(EventCommand command) {
            actuator.actuate(command);
        }
    }
}

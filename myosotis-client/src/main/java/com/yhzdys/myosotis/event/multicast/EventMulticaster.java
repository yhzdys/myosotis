package com.yhzdys.myosotis.event.multicast;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.Listener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.multicast.actuator.Actuator;
import com.yhzdys.myosotis.event.multicast.actuator.ConfigEventActuator;
import com.yhzdys.myosotis.event.multicast.actuator.NamespaceEventActuator;
import com.yhzdys.myosotis.executor.MulticasterExecutor;
import com.yhzdys.myosotis.misc.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventMulticaster {

    private final MulticasterExecutor executor = new MulticasterExecutor();

    /**
     * <namespace, ActuatorWrapper>
     */
    private final Map<String, ActuatorWrapper> namespaceListeners = new ConcurrentHashMap<>(0);

    /**
     * <namespace, <configKey, List<ActuatorWrapper>>>
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
        namespaceListeners.computeIfAbsent(
                listener.namespace(), k -> new ActuatorWrapper(listener, new NamespaceEventActuator(executor))
        );
    }

    public void addConfigListener(ConfigListener listener) {
        configListeners.computeIfAbsent(listener.namespace(), k -> new ConcurrentHashMap<>(2))
                .computeIfAbsent(listener.configKey(), k -> new CopyOnWriteArrayList<>())
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
        actuator.actuate(event.getConfigKey(), event);
    }

    private void triggerConfigListeners(MyosotisEvent event) {
        Map<String, List<ActuatorWrapper>> listenerMap = configListeners.get(event.getNamespace());
        if (MapUtils.isEmpty(listenerMap)) {
            return;
        }
        List<ActuatorWrapper> actuators = listenerMap.get(event.getConfigKey());
        if (CollectionUtils.isEmpty(actuators)) {
            return;
        }
        for (ActuatorWrapper actuator : actuators) {
            actuator.actuate(event);
        }
    }

    private static final class ActuatorWrapper {

        private final Listener listener;
        private final Actuator actuator;

        private ActuatorWrapper(Listener listener, Actuator actuator) {
            this.listener = listener;
            this.actuator = actuator;
        }

        private void actuate(MyosotisEvent event) {
            actuator.actuate(
                    new EventExecutor(() -> this.trigger(listener, event))
            );
        }

        private void actuate(String id, MyosotisEvent event) {
            actuator.actuate(
                    new EventExecutor(id, () -> this.trigger(listener, event))
            );
        }

        private void trigger(Listener listener, MyosotisEvent event) {
            try {
                listener.handle(event);
            } catch (Throwable e) {
                LoggerFactory.getLogger().error("Trigger Listener({}) failed, {}", listener.getClass().getName(), e.getMessage());
            }
        }
    }
}

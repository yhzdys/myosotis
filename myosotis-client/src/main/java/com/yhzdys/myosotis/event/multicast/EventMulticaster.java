package com.yhzdys.myosotis.event.multicast;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.multicast.actuator.ConfigEventActuator;
import com.yhzdys.myosotis.event.multicast.actuator.NamespaceEventActuator;
import com.yhzdys.myosotis.executor.MulticasterExecutor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public final class EventMulticaster {

    private final MulticasterExecutor executor = new MulticasterExecutor();

    /**
     * <namespace, ActuatorWrapper>
     */
    private final Map<String, Set<ActuatorWrapper>> namespaceListeners = new ConcurrentHashMap<>(0);

    /**
     * <namespace + configKey, ActuatorWrapper>
     */
    private final Map<String, Set<ActuatorWrapper>> configListeners = new ConcurrentHashMap<>(0);

    public boolean hasListener(String namespace, String configKey) {
        Set<ActuatorWrapper> wrappers = configListeners.get(this.mapKey(namespace, configKey));
        return wrappers != null && !wrappers.isEmpty();
    }

    public void addNamespaceListener(NamespaceListener listener) {
        namespaceListeners.computeIfAbsent(listener.namespace(), k -> new CopyOnWriteArraySet<>())
                .add(new ActuatorWrapper(listener, new NamespaceEventActuator(executor)));
    }

    public void addConfigListener(ConfigListener listener) {
        String key = this.mapKey(listener.namespace(), listener.configKey());
        configListeners.computeIfAbsent(key, k -> new CopyOnWriteArraySet<>())
                .add(new ActuatorWrapper(listener, new ConfigEventActuator(executor)));
    }

    public void multicast(MyosotisEvent event) {
        this.triggerNamespaceListeners(event);
        this.triggerConfigListeners(event);
    }

    private void triggerNamespaceListeners(MyosotisEvent event) {
        Set<ActuatorWrapper> wrappers = namespaceListeners.get(event.getNamespace());
        if (wrappers == null || wrappers.isEmpty()) {
            return;
        }
        for (ActuatorWrapper wrapper : wrappers) {
            wrapper.actuate(event.getConfigKey(), event);
        }
    }

    private void triggerConfigListeners(MyosotisEvent event) {
        String key = this.mapKey(event.getNamespace(), event.getConfigKey());
        Set<ActuatorWrapper> wrappers = configListeners.get(key);
        if (wrappers == null || wrappers.isEmpty()) {
            return;
        }
        for (ActuatorWrapper wrapper : wrappers) {
            wrapper.actuate(event);
        }
    }

    private String mapKey(String namespace, String configKey) {
        return namespace + ":" + configKey;
    }
}

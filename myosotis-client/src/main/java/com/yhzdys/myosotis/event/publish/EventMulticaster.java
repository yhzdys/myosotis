package com.yhzdys.myosotis.event.publish;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.publish.executor.ConfigListenerExecutor;
import com.yhzdys.myosotis.event.publish.executor.NamespaceListenerExecutor;
import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.misc.LoggerFactory;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * publish config change event
 *
 * @see com.yhzdys.myosotis.event.listener.Listener
 * @see com.yhzdys.myosotis.event.listener.NamespaceListener
 * @see com.yhzdys.myosotis.event.listener.ConfigListener
 */
public final class EventMulticaster {

    /**
     * trigger namespaceListener
     */
    public static void triggerNamespaceListeners(final NamespaceListenerExecutor executor,
                                                 final NamespaceListener namespaceListener,
                                                 final MyosotisEvent event) {
        if (namespaceListener == null) {
            return;
        }
        executor.execute(
                new NamespaceListenerExecutor.Task(
                        event.getConfigKey(), () -> triggerNamespaceListeners(namespaceListener, event)
                )
        );
    }

    /**
     * trigger configListeners
     */
    public static void triggerConfigListeners(final ConfigListenerExecutor executor,
                                              final List<ConfigListener> configListeners,
                                              final MyosotisEvent event) {
        if (CollectionUtils.isEmpty(configListeners)) {
            return;
        }
        for (ConfigListener configListener : configListeners) {
            executor.getExecutor(configListener)
                    .execute(
                            () -> triggerConfigListener(configListener, event)
                    );
        }
    }

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

}
package com.yhzdys.myosotis.event.publish;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.Listener;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.publish.executor.ConfigListenerExecutorFactory;
import com.yhzdys.myosotis.event.publish.executor.NamespaceListenerExecutorFactory;
import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.misc.LoggerFactory;
import com.yhzdys.myosotis.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * publish config change event
 *
 * @see Listener
 * @see NamespaceListener
 * @see ConfigListener
 */
public final class MyosotisEventMulticaster {

    /**
     * trigger namespaceListener
     */
    public static void triggerNamespaceListeners(final NamespaceListenerExecutorFactory executor,
                                                 final NamespaceListener namespaceListener,
                                                 final MyosotisEvent event) {
        if (namespaceListener == null) {
            return;
        }
        executor.execute(
                new NamespaceListenerExecutorFactory.Task(
                        event.getConfigKey(), () -> triggerNamespaceListeners(namespaceListener, event)
                )
        );
    }

    /**
     * trigger configListeners
     */
    public static void triggerConfigListeners(final ConfigListenerExecutorFactory executor,
                                              final List<ConfigListener> configListeners,
                                              final MyosotisEvent event) {
        if (CollectionUtils.isEmpty(configListeners)) {
            return;
        }
        for (ConfigListener configListener : configListeners) {
            executor.getExecutor(ObjectUtil.getId(configListener))
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
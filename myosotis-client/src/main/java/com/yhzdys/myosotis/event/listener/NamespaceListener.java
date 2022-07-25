package com.yhzdys.myosotis.event.listener;

import com.yhzdys.myosotis.event.publish.MyosotisEventMulticaster;

/**
 * namespace config(s) change event listener interface
 * only receive events of defined namespace
 *
 * @see com.yhzdys.myosotis.entity.MyosotisEvent
 * @see MyosotisEventMulticaster
 */
public interface NamespaceListener extends Listener {
}

package com.yhzdys.myosotis.event.listener;

import com.yhzdys.myosotis.event.Listener;
import com.yhzdys.myosotis.event.publish.MyosotisEventMulticaster;

/**
 * single myosotis config change event listener interface
 * only receive events of defined configKey
 *
 * @see com.yhzdys.myosotis.entity.MyosotisEvent
 * @see MyosotisEventMulticaster
 */
public interface ConfigListener extends Listener {

    /**
     * return your configKey want to listen
     */
    String configKey();

}

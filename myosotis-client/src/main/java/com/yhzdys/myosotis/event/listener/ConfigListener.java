package com.yhzdys.myosotis.event.listener;

/**
 * single myosotis config change event listener interface
 * only receive events of defined configKey
 *
 * @see com.yhzdys.myosotis.entity.MyosotisEvent
 * @see com.yhzdys.myosotis.event.multicast.MyosotisEventMulticaster
 */
public interface ConfigListener extends Listener {

    /**
     * return your configKey want to listen
     */
    String configKey();

}

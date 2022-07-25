package com.yhzdys.myosotis.event.listener;

import com.yhzdys.myosotis.entity.MyosotisEvent;

/**
 * myosotis event listener
 * will invoke method handle when config change
 *
 * @see com.yhzdys.myosotis.entity.MyosotisEvent
 * @see com.yhzdys.myosotis.event.multicast.MyosotisEventMulticaster
 */
public interface Listener {

    String namespace();

    /**
     * @param event remote or local config change event
     */
    void handle(MyosotisEvent event);
}

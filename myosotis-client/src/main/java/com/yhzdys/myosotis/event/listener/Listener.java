package com.yhzdys.myosotis.event.listener;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.publish.MyosotisEventMulticaster;

/**
 * myosotis event listener
 * will invoke method handle when config change
 *
 * @see com.yhzdys.myosotis.entity.MyosotisEvent
 * @see MyosotisEventMulticaster
 */
public interface Listener {

    String namespace();

    /**
     * @param event remote or local config change event
     */
    void handle(MyosotisEvent event);
}

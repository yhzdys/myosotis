package com.yhzdys.myosotis.event.listener;

import com.yhzdys.myosotis.entity.MyosotisEvent;

public interface Listener {

    String namespace();

    void handle(MyosotisEvent event);
}

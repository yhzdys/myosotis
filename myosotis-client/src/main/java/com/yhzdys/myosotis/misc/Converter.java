package com.yhzdys.myosotis.misc;

import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.enums.EventType;

/**
 * myosotis config entity converter utility
 */
public final class Converter {

    public static MyosotisEvent config2Event(MyosotisConfig config, EventType type) {
        MyosotisEvent event = new MyosotisEvent();
        event.setId(config.getId());
        event.setNamespace(config.getNamespace());
        event.setConfigKey(config.getConfigKey());
        if (!EventType.DELETE.equals(type)) {
            event.setConfigValue(config.getConfigValue());
            event.setVersion(config.getVersion());
        }
        event.setType(type);
        return event;
    }

    public static MyosotisConfig event2Config(MyosotisEvent event) {
        MyosotisConfig config = new MyosotisConfig();
        config.setId(event.getId());
        config.setNamespace(event.getNamespace());
        config.setConfigKey(event.getConfigKey());
        config.setConfigValue(event.getConfigValue());
        config.setVersion(event.getVersion());
        return config;
    }
}

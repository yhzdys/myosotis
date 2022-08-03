package com.yhzdys.myosotis.processor;

import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;

import java.util.List;

public interface Processor {

    void init(String namespace);

    List<MyosotisEvent> fetchEvents();

    MyosotisConfig getConfig(String namespace, String configKey);

    List<MyosotisConfig> getConfigs(String namespace);

    void save(MyosotisConfig config);
}

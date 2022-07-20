package com.yhzdys.myosotis.processor;

import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * useless... do nothing
 *
 * @see com.yhzdys.myosotis.processor.Processor
 */
public final class UselessProcessor implements Processor {

    @Override
    public void init(String namespace) {
    }

    @Override
    public List<MyosotisEvent> fetchEvents(Map<String, String> cachedConfigs, String namespace) {
        return Collections.emptyList();
    }

    @Override
    public List<MyosotisEvent> pollingEvents() {
        return Collections.emptyList();
    }

    @Override
    public MyosotisConfig getConfig(String namespace, String configKey) {
        return null;
    }

    @Override
    public List<MyosotisConfig> getConfigs(String namespace) {
        return Collections.emptyList();
    }

    @Override
    public List<MyosotisConfig> getConfigs(Map<String, Map<String, Long>> namespaceKeyMap) {
        return Collections.emptyList();
    }

    @Override
    public void save(MyosotisConfig config) {
    }
}

package com.yhzdys.myosotis.processor;

import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;

import java.util.List;
import java.util.Map;

/**
 * config processor
 *
 * @see com.yhzdys.myosotis.processor.LocalProcessor
 * @see com.yhzdys.myosotis.processor.ServerProcessor
 * @see com.yhzdys.myosotis.processor.SnapshotProcessor
 * @see com.yhzdys.myosotis.processor.UselessProcessor
 */
public interface Processor {

    /**
     * create dir of local file
     *
     * @see com.yhzdys.myosotis.processor.LocalProcessor#init(String)
     * @see com.yhzdys.myosotis.processor.SnapshotProcessor#init(String)
     */
    void init(String namespace);

    /**
     * fetch config change events from local
     *
     * @see com.yhzdys.myosotis.processor.LocalProcessor#fetchEvents(Map, String)
     */
    List<MyosotisEvent> fetchEvents(Map<String, String> cachedConfigs, String namespace);

    /**
     * fetch config change events from server
     *
     * @see com.yhzdys.myosotis.processor.ServerProcessor#pollingEvents()
     */
    List<MyosotisEvent> pollingEvents();

    /**
     * query config
     *
     * @see com.yhzdys.myosotis.processor.LocalProcessor#getConfig(String, String)
     * @see com.yhzdys.myosotis.processor.ServerProcessor#getConfig(String, String)
     * @see com.yhzdys.myosotis.processor.SnapshotProcessor#getConfig(String, String)
     */
    MyosotisConfig getConfig(String namespace, String configKey);

    /**
     * query configs
     *
     * @see com.yhzdys.myosotis.processor.ServerProcessor#getConfigs(String)
     */
    List<MyosotisConfig> getConfigs(String namespace);

    /**
     * query configs
     *
     * @param namespaceKeyMap <namespace, <configKey, id>>
     * @see com.yhzdys.myosotis.processor.ServerProcessor#getConfigs(Map)
     */
    List<MyosotisConfig> getConfigs(Map<String, Map<String, Long>> namespaceKeyMap);

    /**
     * save config
     *
     * @see com.yhzdys.myosotis.processor.SnapshotProcessor#save(MyosotisConfig)
     */
    void save(MyosotisConfig config);

}

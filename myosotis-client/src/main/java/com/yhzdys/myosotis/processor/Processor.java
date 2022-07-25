package com.yhzdys.myosotis.processor;

import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;

import java.util.List;

/**
 * config processor
 *
 * @see com.yhzdys.myosotis.processor.ServerProcessor
 * @see com.yhzdys.myosotis.processor.SnapshotProcessor
 */
public interface Processor {

    /**
     * create dir of local file
     *
     * @see com.yhzdys.myosotis.processor.SnapshotProcessor#init(String)
     */
    void init(String namespace);

    /**
     * fetch config change events
     *
     * @see com.yhzdys.myosotis.processor.ServerProcessor#fetchEvents()
     */
    List<MyosotisEvent> fetchEvents();

    /**
     * query config
     *
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
     * save config
     *
     * @see com.yhzdys.myosotis.processor.SnapshotProcessor#save(MyosotisConfig)
     */
    void save(MyosotisConfig config);

}

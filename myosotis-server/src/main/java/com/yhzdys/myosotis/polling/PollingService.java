package com.yhzdys.myosotis.polling;

import com.yhzdys.myosotis.database.mapper.MyosotisConfigMapper;
import com.yhzdys.myosotis.database.object.MyosotisConfigDO;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.enums.EventType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PollingService {

    @Resource
    private MyosotisConfigMapper configMapper;

    private static MyosotisConfig toConfig(MyosotisConfigDO configDO) {
        if (configDO == null) {
            return null;
        }
        MyosotisConfig config = new MyosotisConfig();
        config.setId(configDO.getId());
        config.setNamespace(configDO.getNamespace());
        config.setConfigKey(configDO.getConfigKey());
        config.setConfigValue(configDO.getConfigValue());
        config.setVersion(configDO.getVersion());
        return config;
    }

    private static MyosotisEvent toEvent(MyosotisConfigDO config) {
        MyosotisEvent event = new MyosotisEvent();
        event.setId(config.getId());
        event.setNamespace(config.getNamespace());
        event.setConfigKey(config.getConfigKey());
        event.setConfigValue(config.getConfigValue());
        event.setVersion(config.getVersion());
        return event;
    }

    public List<MyosotisConfig> queryNamespace(String namespace) {
        List<MyosotisConfigDO> configs = configMapper.listByNamespace(namespace);
        return configs.stream()
                .map(PollingService::toConfig)
                .collect(Collectors.toList());
    }

    public MyosotisConfig queryConfig(String namespace, String configKey) {
        MyosotisConfigDO configDO = configMapper.selectByKey(namespace, configKey);
        return configDO == null ? null : toConfig(configDO);
    }

    public List<MyosotisEvent> pollingEvents(List<PollingData> pollingData) {
        List<MyosotisEvent> events = new ArrayList<>();
        for (PollingData data : pollingData) {
            events.addAll(this.fetchEvents(data));
        }
        return events;
    }

    private List<MyosotisEvent> fetchEvents(PollingData pollingData) {
        List<MyosotisConfigDO> configs = this.getConfigs(pollingData);
        return this.fetchEvents(pollingData, configs);
    }

    private List<MyosotisEvent> fetchEvents(PollingData pollingData, List<MyosotisConfigDO> configs) {
        List<MyosotisEvent> events = new ArrayList<>();
        // 拷贝数据，防止线程恢复后重新使用时，数据已经修改过了
        Map<String, Integer> data = new HashMap<>(pollingData.getData());
        for (MyosotisConfigDO config : configs) {
            Integer pollingVersion = data.get(config.getConfigKey());
            // add
            if (pollingVersion == null || pollingVersion < 1) {
                events.add(toEvent(config).setType(EventType.ADD));
            }
            // update
            else if (!config.getVersion().equals(pollingVersion)) {
                events.add(toEvent(config).setType(EventType.UPDATE));
            }
            // 剩下的就是 delete config
            data.remove(config.getConfigKey());
        }
        // delete
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            Integer pollingVersion = entry.getValue();
            if (pollingVersion == null || pollingVersion < 1) {
                continue;
            }
            events.add(
                    new MyosotisEvent(pollingData.getNamespace(), entry.getKey(), EventType.DELETE)
            );
        }
        return events;
    }

    private List<MyosotisConfigDO> getConfigs(PollingData data) {
        if (data.isAll()) {
            return configMapper.listByNamespace(data.getNamespace());
        }
        if (data.getData() == null || data.getData().isEmpty()) {
            return Collections.emptyList();
        }
        return configMapper.listByKeys(data.getNamespace(), data.getData().keySet());
    }

}

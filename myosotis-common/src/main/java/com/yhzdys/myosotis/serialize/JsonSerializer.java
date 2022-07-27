package com.yhzdys.myosotis.serialize;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.misc.JsonUtil;

import java.util.List;

/**
 * serializer of JSON
 */
public final class JsonSerializer implements Serializer {

    private final TypeReference<List<PollingData>> polling_list_tr;
    private final TypeReference<List<MyosotisEvent>> events_tr;
    private final TypeReference<List<MyosotisConfig>> configs_tr;

    public JsonSerializer() {
        polling_list_tr = new TypeReference<List<PollingData>>() {
        };
        events_tr = new TypeReference<List<MyosotisEvent>>() {
        };
        configs_tr = new TypeReference<List<MyosotisConfig>>() {
        };
    }

    @Override
    public byte[] serializePollingData(List<PollingData> list) {
        return JsonUtil.toBytes(list);
    }

    @Override
    public List<PollingData> deserializePollingData(byte[] data) {
        return JsonUtil.toList(data, polling_list_tr);
    }

    @Override
    public byte[] serializeEvents(List<MyosotisEvent> list) {
        return JsonUtil.toBytes(list);
    }

    @Override
    public List<MyosotisEvent> deserializeEvents(byte[] data) {
        return JsonUtil.toList(data, events_tr);
    }

    @Override
    public byte[] serializeConfigs(List<MyosotisConfig> list) {
        return JsonUtil.toBytes(list);
    }

    @Override
    public List<MyosotisConfig> deserializeConfigs(byte[] data) {
        return JsonUtil.toList(data, configs_tr);
    }
}

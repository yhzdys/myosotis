package com.yhzdys.myosotis.serialize;

import com.alibaba.fastjson2.JSON;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class JsonSerializer implements Serializer {
    @Override
    public byte[] serializePollingData(List<PollingData> list) {
        return JSON.toJSONString(list).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public List<PollingData> deserializePollingData(byte[] data) {
        return JSON.parseArray(new String(data, StandardCharsets.UTF_8), PollingData.class);
    }

    @Override
    public byte[] serializeEvents(List<MyosotisEvent> list) {
        return JSON.toJSONString(list).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public List<MyosotisEvent> deserializeEvents(byte[] data) {
        return JSON.parseArray(new String(data, StandardCharsets.UTF_8), MyosotisEvent.class);
    }

    @Override
    public byte[] serializeConfigs(List<MyosotisConfig> list) {
        return JSON.toJSONString(list).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public List<MyosotisConfig> deserializeConfigs(byte[] data) {
        return JSON.parseArray(new String(data, StandardCharsets.UTF_8), MyosotisConfig.class);
    }
}

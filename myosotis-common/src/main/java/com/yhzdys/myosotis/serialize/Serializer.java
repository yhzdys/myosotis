package com.yhzdys.myosotis.serialize;

import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;

import java.util.List;

/**
 * serializer interface
 */
public interface Serializer {

    byte[] serializePollingData(List<PollingData> list) throws Exception;

    List<PollingData> deserializePollingData(byte[] data) throws Exception;

    byte[] serializeEvents(List<MyosotisEvent> list) throws Exception;

    List<MyosotisEvent> deserializeEvents(byte[] data) throws Exception;

    byte[] serializeConfigs(List<MyosotisConfig> list) throws Exception;

    List<MyosotisConfig> deserializeConfigs(byte[] data) throws Exception;

}

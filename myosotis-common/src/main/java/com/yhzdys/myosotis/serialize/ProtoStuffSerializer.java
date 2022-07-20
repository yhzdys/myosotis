package com.yhzdys.myosotis.serialize;

import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public final class ProtoStuffSerializer implements Serializer {

    private final Schema<PollingData> polling_schema;
    private final Schema<MyosotisEvent> event_schema;
    private final Schema<MyosotisConfig> config_schema;

    public ProtoStuffSerializer() {
        this.polling_schema = RuntimeSchema.getSchema(PollingData.class);
        this.event_schema = RuntimeSchema.getSchema(MyosotisEvent.class);
        this.config_schema = RuntimeSchema.getSchema(MyosotisConfig.class);
    }

    @Override
    public byte[] serializePollingData(List<PollingData> list) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ProtobufIOUtil.writeListTo(outputStream, list, polling_schema, LinkedBuffer.allocate());
        return outputStream.toByteArray();
    }

    @Override
    public List<PollingData> deserializePollingData(byte[] data) throws Exception {
        return ProtobufIOUtil.parseListFrom(new ByteArrayInputStream(data), polling_schema);
    }

    @Override
    public byte[] serializeEvents(List<MyosotisEvent> list) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ProtobufIOUtil.writeListTo(outputStream, list, event_schema, LinkedBuffer.allocate());
        return outputStream.toByteArray();
    }

    @Override
    public List<MyosotisEvent> deserializeEvents(byte[] data) throws Exception {
        return ProtobufIOUtil.parseListFrom(new ByteArrayInputStream(data), event_schema);
    }

    @Override
    public byte[] serializeConfigs(List<MyosotisConfig> list) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ProtobufIOUtil.writeListTo(outputStream, list, config_schema, LinkedBuffer.allocate());
        return outputStream.toByteArray();
    }

    @Override
    public List<MyosotisConfig> deserializeConfigs(byte[] data) throws Exception {
        return ProtobufIOUtil.parseListFrom(new ByteArrayInputStream(data), config_schema);
    }
}

package com.yhzdys.myosotis.serialize;

import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.enums.EventType;
import com.yhzdys.myosotis.exception.MyosotisException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.util.Utf8;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AvroSerializer implements Serializer {

    private final Schema polling_list_schema;
    private final Schema polling_schema;
    private final DatumWriter<GenericArray<GenericRecord>> polling_list_writer;
    private final DatumReader<GenericArray<GenericRecord>> polling_list_reader;

    private final Schema events_schema;
    private final Schema event_schema;
    private final DatumWriter<GenericArray<GenericRecord>> events_writer;
    private final DatumReader<GenericArray<GenericRecord>> events_reader;

    private final Schema configs_schema;
    private final Schema config_schema;
    private final DatumWriter<GenericArray<GenericRecord>> configs_writer;
    private final DatumReader<GenericArray<GenericRecord>> configs_reader;

    private final String k_id = "id";
    private final String k_namespace = "namespace";
    private final String k_config_key = "configKey";
    private final String k_config_value = "configValue";
    private final String k_version = "version";
    private final String k_type = "type";
    private final String k_is_all = "isAll";
    private final String k_data = "data";

    public AvroSerializer() {
        try {
            Schema.Parser parser = new Schema.Parser();

            this.polling_list_schema = parser.parse(loadAvsc("schema/polling_data.avsc"));
            this.polling_schema = this.polling_list_schema.getElementType();
            this.polling_list_writer = new GenericDatumWriter<>(this.polling_list_schema);
            this.polling_list_reader = new GenericDatumReader<>(this.polling_list_schema);

            this.events_schema = parser.parse(loadAvsc("schema/events.avsc"));
            this.event_schema = this.events_schema.getElementType();
            this.events_writer = new GenericDatumWriter<>(this.events_schema);
            this.events_reader = new GenericDatumReader<>(this.events_schema);

            this.configs_schema = parser.parse(loadAvsc("schema/configs.avsc"));
            this.config_schema = this.configs_schema.getElementType();
            this.configs_writer = new GenericDatumWriter<>(this.configs_schema);
            this.configs_reader = new GenericDatumReader<>(this.configs_schema);
        } catch (Throwable e) {
            throw new MyosotisException("Initialize schemas failed.", e);
        }
    }

    private InputStream loadAvsc(String schemaPath) throws Throwable {
        URL url = AvroSerializer.class.getClassLoader().getResource(schemaPath);
        if (url == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                url = classLoader.getResource(schemaPath);
            }
        }
        if (url == null) {
            url = ClassLoader.getSystemClassLoader().getResource(schemaPath);
        }
        if (url == null) {
            throw new MyosotisException("Can not load avsc file: " + schemaPath);
        }
        return url.openStream();
    }

    @Override
    public byte[] serializePollingData(List<PollingData> list) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        GenericArray<GenericRecord> recordArray = new GenericData.Array<>(list.size(), polling_list_schema);
        for (PollingData pollingData : list) {
            GenericRecord record = new GenericData.Record(polling_schema);
            record.put(k_is_all, pollingData.isAll());
            record.put(k_namespace, pollingData.getNamespace());
            record.put(k_data, pollingData.getData());
            recordArray.add(record);
        }

        polling_list_writer.write(recordArray, encoder);
        encoder.flush();
        out.close();
        return out.toByteArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PollingData> deserializePollingData(byte[] data) throws Exception {
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        GenericArray<GenericRecord> recordArray = polling_list_reader.read(null, decoder);

        List<PollingData> list = new ArrayList<>();
        for (GenericRecord record : recordArray) {
            PollingData pollingData = new PollingData();
            pollingData.setAll((boolean) record.get(k_is_all));
            pollingData.setNamespace(record.get(k_namespace).toString());
            Map<Utf8, Integer> dataTempMap = (Map<Utf8, Integer>) record.get(k_data);
            Map<String, Integer> dataMap = new HashMap<>(dataTempMap.size());
            for (Map.Entry<Utf8, Integer> entry : dataTempMap.entrySet()) {
                dataMap.put(entry.getKey().toString(), entry.getValue());
            }
            pollingData.setData(dataMap);
            list.add(pollingData);
        }
        return list;
    }

    @Override
    public byte[] serializeEvents(List<MyosotisEvent> list) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);

        GenericArray<GenericRecord> recordArray = new GenericData.Array<>(list.size(), events_schema);
        for (MyosotisEvent event : list) {
            GenericRecord record = new GenericData.Record(event_schema);
            recordArray.add(record);

            record.put(k_id, event.getId());
            record.put(k_namespace, event.getNamespace());
            record.put(k_config_key, event.getConfigKey());
            record.put(k_type, event.getType().toString());
            // 删除模式下，无需传递下面的字段
            if (EventType.DELETE.equals(event.getType())) {
                continue;
            }
            record.put(k_config_value, event.getConfigValue());
            record.put(k_version, event.getVersion());
        }

        events_writer.write(recordArray, encoder);
        encoder.flush();
        outputStream.close();
        return outputStream.toByteArray();
    }

    @Override
    public List<MyosotisEvent> deserializeEvents(byte[] data) throws Exception {
        if (ArrayUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        GenericArray<GenericRecord> recordArray = events_reader.read(null, decoder);

        List<MyosotisEvent> eventList = new ArrayList<>();
        for (GenericRecord record : recordArray) {
            Object id = record.get(k_id);
            Object namespace = record.get(k_namespace);
            Object configKey = record.get(k_config_key);
            Object configValue = record.get(k_config_value);
            Object version = record.get(k_version);
            Object type = record.get(k_type);
            eventList.add(
                    new MyosotisEvent()
                            .setId(id == null ? null : (Long) id)
                            .setNamespace(namespace == null ? null : namespace.toString())
                            .setConfigKey(configKey == null ? null : configKey.toString())
                            .setConfigValue(configValue == null ? null : configValue.toString())
                            .setVersion(version == null ? 0 : (Integer) version)
                            .setType(EventType.valueOf(type.toString()))
            );
        }
        return eventList;
    }

    @Override
    public byte[] serializeConfigs(List<MyosotisConfig> list) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);

        GenericArray<GenericRecord> recordArray = new GenericData.Array<>(list.size(), configs_schema);
        for (MyosotisConfig config : list) {
            GenericRecord record = new GenericData.Record(config_schema);
            record.put(k_id, config.getId());
            record.put(k_namespace, config.getNamespace());
            record.put(k_config_key, config.getConfigKey());
            record.put(k_config_value, config.getConfigValue());
            record.put(k_version, config.getVersion());
            recordArray.add(record);
        }

        configs_writer.write(recordArray, encoder);
        encoder.flush();
        outputStream.close();
        return outputStream.toByteArray();
    }

    @Override
    public List<MyosotisConfig> deserializeConfigs(byte[] data) throws Exception {
        if (ArrayUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        GenericArray<GenericRecord> recordArray = configs_reader.read(null, decoder);

        List<MyosotisConfig> configList = new ArrayList<>();
        for (GenericRecord record : recordArray) {
            Object id = record.get(k_id);
            Object namespace = record.get(k_namespace).toString();
            String configKey = record.get(k_config_key).toString();
            Object configValue = record.get(k_config_value);
            Object version = record.get(k_version);
            configList.add(
                    new MyosotisConfig()
                            .setId(id == null ? null : (Long) id)
                            .setNamespace(namespace.toString())
                            .setConfigKey(configKey)
                            .setConfigValue(configValue == null ? null : configValue.toString())
                            .setVersion(version == null ? 0 : (Integer) version)
            );
        }
        return configList;
    }
}

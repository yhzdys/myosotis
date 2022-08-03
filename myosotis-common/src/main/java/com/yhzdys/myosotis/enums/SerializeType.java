package com.yhzdys.myosotis.enums;

import com.yhzdys.myosotis.exception.MyosotisException;
import com.yhzdys.myosotis.serialize.AvroSerializer;
import com.yhzdys.myosotis.serialize.JsonSerializer;
import com.yhzdys.myosotis.serialize.ProtoStuffSerializer;
import com.yhzdys.myosotis.serialize.Serializer;
import org.apache.commons.lang3.StringUtils;

public enum SerializeType {

    JSON("json", new JsonSerializer()),
    AVRO("avro", new AvroSerializer()),
    PROTOSTUFF("protostuff", new ProtoStuffSerializer()),
    ;

    private final String code;
    private final Serializer serializer;

    SerializeType(String code, Serializer serializer) {
        this.code = code;
        this.serializer = serializer;
    }

    public static SerializeType codeOf(String code) {
        if (StringUtils.isEmpty(code)) {
            throw new MyosotisException("Serializer code may not be null");
        }
        for (SerializeType serializeType : SerializeType.values()) {
            if (serializeType.getCode().equalsIgnoreCase(code)) {
                return serializeType;
            }
        }
        throw new MyosotisException("Unknown serialize type");
    }

    public String getCode() {
        return code;
    }

    public Serializer getSerializer() {
        return serializer;
    }
}

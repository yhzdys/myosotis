package com.yhzdys.myosotis.misc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhzdys.myosotis.exception.MyosotisException;

import java.util.List;

/**
 * utility of JSON
 */
public final class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public static String toString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new MyosotisException("JSON stringify error", e);
        }
    }

    public static byte[] toBytes(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (Exception e) {
            throw new MyosotisException("JSON serialize object error", e);
        }
    }

    public static <T> T toObject(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new MyosotisException("JSON parse object error", e);
        }
    }

    public static <T> List<T> toList(byte[] bytes, TypeReference<List<T>> typeReference) {
        try {
            return objectMapper.readValue(bytes, typeReference);
        } catch (Exception e) {
            throw new MyosotisException("JSON parse object(s) error", e);
        }
    }
}

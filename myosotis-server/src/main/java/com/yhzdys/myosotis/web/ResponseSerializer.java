package com.yhzdys.myosotis.web;

import com.yhzdys.myosotis.compress.Lz4;
import com.yhzdys.myosotis.config.server.ServerConfig;
import com.yhzdys.myosotis.config.server.ServerConfigLoader;
import com.yhzdys.myosotis.constant.NetConst;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.enums.SerializeType;
import com.yhzdys.myosotis.exception.MyosotisException;
import com.yhzdys.myosotis.serialize.Serializer;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

public class ResponseSerializer {

    private static final Logger logger = LoggerFactory.getLogger(ResponseSerializer.class);

    private static final byte[] empty_byte_array = new byte[0];

    public static byte[] events(List<MyosotisEvent> events) {
        if (CollectionUtils.isEmpty(events)) {
            return empty_byte_array;
        }
        return compress(serializeEvents(events));
    }

    public static byte[] config(MyosotisConfig config) {
        if (config == null) {
            set404Response();
            return empty_byte_array;
        }
        return compress(serializeConfigs(Collections.singletonList(config)));
    }

    public static byte[] configs(List<MyosotisConfig> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            set404Response();
            return empty_byte_array;
        }
        return compress(serializeConfigs(configs));
    }

    private static byte[] serializeEvents(List<MyosotisEvent> events) {
        try {
            return getSerializer().serializeEvents(events);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return empty_byte_array;
        }
    }

    private static byte[] serializeConfigs(List<MyosotisConfig> configs) {
        try {
            return getSerializer().serializeConfigs(configs);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return empty_byte_array;
        }
    }

    private static Serializer getSerializer() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new MyosotisException("Servlet request attributes is null");
        }
        SerializeType serializeType;
        HttpServletRequest request = attributes.getRequest();
        // client support avro
        if (NetConst.support_yes.equalsIgnoreCase(request.getHeader(NetConst.serialize_avro_support))) {
            serializeType = SerializeType.AVRO;
        } else {
            serializeType = SerializeType.JSON;
        }
        HttpServletResponse response = attributes.getResponse();
        if (response == null) {
            throw new MyosotisException("Http servlet response is null");
        }
        response.addHeader(NetConst.serialize_type, serializeType.getCode());
        return serializeType.getSerializer();
    }

    private static byte[] compress(byte[] data) {
        ServerConfig config = ServerConfigLoader.get();
        if (!config.isEnableCompress() || data.length < config.getCompressThreshold()) {
            return data;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new MyosotisException("Servlet request attributes is null");
        }
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        if (response == null) {
            throw new MyosotisException("Http servlet response is null");
        }
        // client support data compress
        if (NetConst.support_yes.equalsIgnoreCase(request.getHeader(NetConst.compress_support))) {
            byte[] compressed = Lz4.compress(data);
            response.setHeader(NetConst.origin_data_length, String.valueOf(data.length));
            return compressed;
        }
        return data;
    }

    private static void set404Response() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletResponse response = attributes.getResponse();
        if (response == null) {
            return;
        }
        response.setStatus(404);
    }
}

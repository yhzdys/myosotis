package com.yhzdys.myosotis.processor;

import com.yhzdys.myosotis.MyosotisCustomizer;
import com.yhzdys.myosotis.compress.Lz4;
import com.yhzdys.myosotis.constant.NetConst;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.enums.SerializeType;
import com.yhzdys.myosotis.exception.MyosotisException;
import com.yhzdys.myosotis.metadata.AbsentConfigMetadata;
import com.yhzdys.myosotis.metadata.PollingConfigMetadata;
import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.misc.LoggerFactory;
import com.yhzdys.myosotis.misc.MyosotisHttpClient;
import com.yhzdys.myosotis.serialize.Serializer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * query server config and fetch config change events
 *
 * @see com.yhzdys.myosotis.processor.Processor
 */
public final class ServerProcessor implements Processor {

    /**
     * shared http client
     */
    private static final MyosotisHttpClient myosotisHttpClient = MyosotisHttpClient.getInstance();

    private final String serverAddress;

    private final ExceptionCounter counter;

    /**
     * config metadata from clientManger
     */
    private final PollingConfigMetadata pollingConfigMetaData;
    private final AbsentConfigMetadata absentConfigMetaData;

    private final HttpPost pollingPost;

    private final SerializeType serializeType;
    private final boolean enableCompress;
    private final long compressThreshold;

    private long lastModifiedVersion = 0;

    public ServerProcessor(final MyosotisCustomizer customizer,
                           final PollingConfigMetadata pollingConfigMetaData,
                           final AbsentConfigMetadata absentConfigMetaData) {
        if (StringUtils.isEmpty(customizer.getServerAddress())) {
            throw new MyosotisException("Myosotis server address may not be null");
        }

        this.serverAddress = customizer.getServerAddress();
        this.serializeType = customizer.getSerializeType();
        this.enableCompress = customizer.isEnableCompress();
        this.compressThreshold = customizer.getCompressThreshold();

        this.counter = new ExceptionCounter();
        this.pollingConfigMetaData = pollingConfigMetaData;
        this.absentConfigMetaData = absentConfigMetaData;
        this.pollingPost = new HttpPost();
        try {
            this.pollingPost.setURI(new URI(serverAddress + NetConst.URL.polling));
        } catch (Exception e) {
            throw new MyosotisException(e);
        }
        // TODO client language & version support
        this.pollingPost.addHeader(NetConst.client_language, "java");
        this.pollingPost.addHeader(NetConst.client_version, "1.0");
        //  add feature support headers
        this.addFeatureSupportHeader(pollingPost);
        // customized serialize type
        this.pollingPost.addHeader(NetConst.serialize_type, customizer.getSerializeType().getCode());
        this.pollingPost.setConfig(NetConst.long_polling_config);
    }

    private void reuse(CloseableHttpResponse response) {
        if (response == null) {
            return;
        }
        try {
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            LoggerFactory.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public void init(String namespace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MyosotisEvent> fetchEvents(Map<String, String> cachedConfigs, String namespace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MyosotisEvent> pollingEvents() {
        CloseableHttpResponse response = null;
        try {
            response = myosotisHttpClient.execute(this.pollingPost());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                counter.reset();
                return this.deserializeEvents(response);
            }
            counter.increase(statusCode);
        } catch (Exception e) {
            LoggerFactory.getLogger().info("Polling failed. error: {}", e.getMessage(), e);
            counter.increase(500);
        } finally {
            this.reuse(response);
        }
        return null;
    }

    @Override
    public MyosotisConfig getConfig(String namespace, String configKey) {
        CloseableHttpResponse response = null;
        try {
            response = myosotisHttpClient.execute(this.queryGet(namespace, configKey));
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return this.deserializeConfig(response);
            }
            if (statusCode == 404) {
                absentConfigMetaData.add(namespace, configKey);
            }
            return null;
        } catch (Throwable e) {
            LoggerFactory.getLogger().info("Get config failed. error: {}", e.getMessage());
        } finally {
            this.reuse(response);
        }
        return null;
    }

    @Override
    public List<MyosotisConfig> getConfigs(String namespace) {
        CloseableHttpResponse response = null;
        try {
            response = myosotisHttpClient.execute(this.queryGet(namespace, null));
            return this.deserializeConfigs(response);
        } catch (Throwable e) {
            LoggerFactory.getLogger().info("Get config(s) failed. error: {}", e.getMessage());
        } finally {
            this.reuse(response);
        }
        return null;
    }

    @Override
    public List<MyosotisConfig> getConfigs(Map<String, Map<String, Long>> namespaceKeyMap) {
        CloseableHttpResponse response = null;
        try {
            response = myosotisHttpClient.execute(this.queryPost(namespaceKeyMap));
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 404) {
                return null;
            }
            if (statusCode == 200) {
                return this.deserializeConfigs(response);
            }
        } catch (Throwable e) {
            LoggerFactory.getLogger().info("Get config(s) failed. error: {}", e.getMessage());
        } finally {
            this.reuse(response);
        }
        return null;
    }

    @Override
    public void save(MyosotisConfig config) {
        throw new UnsupportedOperationException();
    }

    private HttpPost pollingPost() throws Exception {
        long currentModifiedVersion = pollingConfigMetaData.getModifiedVersion();
        // <id, version>没有变化,重用之前的数据
        if (lastModifiedVersion >= currentModifiedVersion) {
            return pollingPost;
        }
        lastModifiedVersion = currentModifiedVersion;
        Collection<PollingData> pollingData = pollingConfigMetaData.getPollingMap().values();
        List<PollingData> pollingList = new ArrayList<>(pollingData);
        // clear header
        pollingPost.removeHeaders(NetConst.origin_data_length);
        // serialization
        byte[] data = serializeType.getSerializer().serializePollingData(pollingList);
        // data compress
        ByteArrayEntity byteArrayEntity;
        if (enableCompress && data.length >= compressThreshold) {
            pollingPost.addHeader(NetConst.origin_data_length, String.valueOf(data.length));
            byteArrayEntity = new ByteArrayEntity(Lz4.compress(data));
        } else {
            byteArrayEntity = new ByteArrayEntity(data);
        }

        pollingPost.setEntity(byteArrayEntity);
        if (pollingConfigMetaData.getModifiedVersion() != lastModifiedVersion) {
            LoggerFactory.getLogger().warn("Config changed after polling");
        }
        return pollingPost;
    }

    public HttpGet queryGet(String namespace, String configKey) throws Exception {
        HttpGet request = new HttpGet(
                new URI(serverAddress + NetConst.URL.configQuery(namespace, configKey))
        );
        this.addFeatureSupportHeader(request);

        request.setConfig(NetConst.default_config);
        return request;
    }

    public HttpPost queryPost(Map<String, Map<String, Long>> namespaceKeyMap) throws Exception {
        HttpPost request = new HttpPost(
                new URI(serverAddress + NetConst.URL.query_configs)
        );
        request.addHeader(NetConst.header_content_json);
        this.addFeatureSupportHeader(request);

        request.setConfig(NetConst.default_config);
        request.setEntity(new StringEntity(JsonUtil.toString(namespaceKeyMap)));
        return request;
    }

    private void addFeatureSupportHeader(HttpRequestBase request) {
        request.addHeader(NetConst.compress_support, NetConst.support_yes);
        request.addHeader(NetConst.serialize_avro_support, NetConst.support_yes);
        request.addHeader(NetConst.serialize_protostuff_support, NetConst.support_yes);
    }

    private List<MyosotisEvent> deserializeEvents(CloseableHttpResponse response) throws Exception {
        byte[] data = this.getResponseData(response);
        if (ArrayUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        return this.getSerializer(response).deserializeEvents(data);
    }

    private MyosotisConfig deserializeConfig(CloseableHttpResponse response) throws Exception {
        List<MyosotisConfig> configs = this.deserializeConfigs(response);
        if (CollectionUtils.isEmpty(configs)) {
            return null;
        }
        return configs.get(0);
    }

    private List<MyosotisConfig> deserializeConfigs(CloseableHttpResponse response) throws Exception {
        byte[] data = this.getResponseData(response);
        if (ArrayUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        return this.getSerializer(response).deserializeConfigs(data);
    }

    /**
     * 获取服务端返回的数据，如果数据是压缩的，还要对数据进行解压缩
     */
    private byte[] getResponseData(HttpResponse response) throws Exception {
        // 判断服务端返回的数据是否是压缩后的结果
        Header header = response.getLastHeader(NetConst.origin_data_length);
        HttpEntity entity = response.getEntity();
        byte[] data = new byte[0];
        if (entity != null) {
            if (header != null) {
                // 对压缩的数据进行解压缩
                byte[] bytes = EntityUtils.toByteArray(entity);
                data = Lz4.decompress(bytes, Integer.parseInt(header.getValue()));
            } else {
                data = EntityUtils.toByteArray(entity);
            }
        }
        return data;
    }

    private Serializer getSerializer(HttpResponse response) {
        Header header = response.getFirstHeader(NetConst.serialize_type);
        return SerializeType.codeOf(header.getValue()).getSerializer();
    }

    /**
     * record the number of exceptions
     */
    private static class ExceptionCounter {
        private int count = 0;

        public void increase(int statusCode) {
            count++;
            if (count >= 10) {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(60));
                return;
            }
            if (statusCode == 500) {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
            }
            if (statusCode == 400) {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
            }
        }

        public void reset() {
            count = 0;
        }
    }

}

package com.yhzdys.myosotis.processor;

import com.yhzdys.myosotis.Config;
import com.yhzdys.myosotis.compress.Lz4;
import com.yhzdys.myosotis.constant.NetConst;
import com.yhzdys.myosotis.constant.SystemConst;
import com.yhzdys.myosotis.data.ConfigMetadata;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.enums.SerializeType;
import com.yhzdys.myosotis.exception.MyosotisException;
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
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public final class ServerProcessor implements Processor {

    private static final MyosotisHttpClient myosotisHttpClient = MyosotisHttpClient.getInstance();

    private final String serverAddress;

    private final ExceptionCounter counter;
    private final ConfigMetadata configMetadata;
    private final HttpPost pollingPost;

    private final SerializeType serializeType;
    private final boolean enableCompress;
    private final long compressThreshold;

    private long version = 0;

    public ServerProcessor(Config config, ConfigMetadata configMetadata) {
        if (StringUtils.isEmpty(config.getServerAddress())) {
            throw new MyosotisException("Myosotis server address may not be null");
        }
        this.serverAddress = config.getServerAddress();
        this.serializeType = config.getSerializeType();
        this.enableCompress = config.isEnableCompress();
        this.compressThreshold = config.getCompressThreshold();

        this.counter = new ExceptionCounter();
        this.configMetadata = configMetadata;
        this.pollingPost = new HttpPost();
        try {
            this.pollingPost.setURI(new URI(serverAddress + NetConst.URL.polling));
        } catch (Exception e) {
            throw new MyosotisException(e);
        }
        this.addCommonHeader(pollingPost);
        // 1.1默认为长连接 这里显式声明
        this.pollingPost.setHeader(NetConst.header_long_connection);
        this.pollingPost.setConfig(NetConst.long_polling_config);
    }

    @Override
    public void init(String namespace) {
    }

    @Override
    public List<MyosotisEvent> fetchEvents() {
        CloseableHttpResponse response = null;
        try {
            response = myosotisHttpClient.execute(this.pollingPost());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                counter.reset();
                return this.deserializeEvents(response);
            }
            if (statusCode == 503) {
                LoggerFactory.getLogger().error("Too many client connections");
            }
            counter.increase();
        } catch (Exception e) {
            LoggerFactory.getLogger().error("Polling failed, msg: {}", e.getMessage());
            counter.increase();
        } finally {
            this.consume(response);
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
                configMetadata.addAbsent(namespace, configKey);
            }
            return null;
        } catch (Throwable e) {
            LoggerFactory.getLogger().error("Get config failed, msg: {}", e.getMessage());
        } finally {
            this.consume(response);
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
            LoggerFactory.getLogger().error("Get config(s) failed, msg: {}", e.getMessage());
        } finally {
            this.consume(response);
        }
        return null;
    }

    @Override
    public void save(MyosotisConfig config) {
    }

    private HttpPost pollingPost() throws Exception {
        long currentModifiedVersion = configMetadata.pollingVersion();
        // version not changed, reuse previous data
        if (version >= currentModifiedVersion) {
            return pollingPost;
        }
        version = currentModifiedVersion;
        Collection<PollingData> pollingData = configMetadata.pollingData();
        List<PollingData> pollingList = new ArrayList<>(pollingData);
        // serialization
        byte[] data = serializeType.getSerializer().serializePollingData(pollingList);
        // data compress
        ByteArrayEntity byteArrayEntity;
        if (enableCompress && data.length >= compressThreshold) {
            pollingPost.setHeader(NetConst.origin_data_length, String.valueOf(data.length));
            byteArrayEntity = new ByteArrayEntity(Lz4.compress(data));
        } else {
            pollingPost.removeHeaders(NetConst.origin_data_length);
            byteArrayEntity = new ByteArrayEntity(data);
        }
        pollingPost.setEntity(byteArrayEntity);
        return pollingPost;
    }

    public HttpGet queryGet(String namespace, String configKey) throws Exception {
        HttpGet request = new HttpGet(new URI(serverAddress + NetConst.URL.query(namespace, configKey)));
        this.addCommonHeader(request);
        request.setHeader(NetConst.header_short_connection);
        return request;
    }

    private void addCommonHeader(HttpRequestBase request) {
        request.setHeader(NetConst.client_ip, SystemConst.local_host);
        request.setHeader(NetConst.client_language, "java");
        request.setHeader(NetConst.serialize_type, serializeType.getCode());
        request.setHeader(NetConst.compress_support, NetConst.support_yes);
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

    private byte[] getResponseData(HttpResponse response) throws Exception {
        Header header = response.getFirstHeader(NetConst.origin_data_length);
        HttpEntity entity = response.getEntity();
        return header == null ? EntityUtils.toByteArray(entity) :
                Lz4.decompress(EntityUtils.toByteArray(entity), Integer.parseInt(header.getValue()));
    }

    private Serializer getSerializer(HttpResponse response) {
        Header header = response.getFirstHeader(NetConst.serialize_type);
        return SerializeType.codeOf(header.getValue()).getSerializer();
    }

    private void consume(CloseableHttpResponse response) {
        if (response == null) {
            return;
        }
        try {
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            LoggerFactory.getLogger().error(e.getMessage(), e);
        }
    }

    private static class ExceptionCounter {
        private int count = 0;

        public void increase() {
            count++;
            if (count >= 10) {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(30));
                return;
            }
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
        }

        public void reset() {
            count = 0;
        }
    }
}

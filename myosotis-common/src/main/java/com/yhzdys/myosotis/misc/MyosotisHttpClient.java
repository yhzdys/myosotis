package com.yhzdys.myosotis.misc;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * for myosotis internal use only
 * not expose {@link Closeable#close()} method, so current client can't be close(design on purpose)
 */
public final class MyosotisHttpClient {

    private static final MyosotisHttpClient instance = new MyosotisHttpClient(createClient());
    private final CloseableHttpClient httpClient;

    private MyosotisHttpClient(CloseableHttpClient client) {
        this.httpClient = client;
    }

    public static MyosotisHttpClient getInstance() {
        return instance;
    }

    private static CloseableHttpClient createClient() {
        try {
            return HttpClientBuilder.create()
                    .setMaxConnTotal(1024)
                    .setMaxConnPerRoute(1024)
                    .evictIdleConnections(60, TimeUnit.SECONDS)
                    .setKeepAliveStrategy(new MyosotisKeepAliveStrategy())
                    .build();
        } catch (Throwable t) {
            LoggerFactory.getLogger().error(t.getMessage(), t);
        }
        return null;
    }

    public CloseableHttpResponse execute(HttpUriRequest request) throws Exception {
        return httpClient.execute(request);
    }

    /**
     * copy from {@link org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy}
     */
    private static class MyosotisKeepAliveStrategy implements ConnectionKeepAliveStrategy {

        @Override
        public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
            final HeaderElementIterator it = new BasicHeaderElementIterator(httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement header = it.nextElement();
                String name = header.getName();
                String value = header.getValue();
                if (value != null && name.equalsIgnoreCase("timeout")) {
                    try {
                        // 90%
                        return Long.parseLong(value) * 900;
                    } catch (Exception ignore) {
                    }
                }
            }
            return -1;
        }
    }
}

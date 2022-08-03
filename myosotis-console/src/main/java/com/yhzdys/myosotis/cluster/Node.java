package com.yhzdys.myosotis.cluster;

import com.yhzdys.myosotis.constant.NetConst;
import com.yhzdys.myosotis.constant.SystemConst;
import com.yhzdys.myosotis.misc.MyosotisHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Node {

    private static final Logger logger = LoggerFactory.getLogger(Node.class);

    private static final String health_check = "/cluster/check";
    private static final String polling_notify = "/cluster/polling/notify/";

    /**
     * lazy health check threshold (10min.)
     */
    private static final long lazy_check_threshold = TimeUnit.MINUTES.toMillis(10) - 1L;

    private final MyosotisHttpClient myosotisHttpClient = MyosotisHttpClient.getInstance();

    private final String address;

    private final HttpGet monitorRequest;

    private long lastCheckTime;

    private boolean health;

    private boolean lazyCheck;

    private long failCount;

    public Node(String address) {
        this.address = address;
        HttpGet request = new HttpGet("http://" + address + health_check);
        request.setConfig(NetConst.default_config);
        request.addHeader(NetConst.client_host_ip, SystemConst.local_host);
        this.monitorRequest = request;
        this.health = true;
        this.lazyCheck = false;
        this.failCount = 0;
    }

    public void healthCheck() {
        long now = System.currentTimeMillis();
        // to lazy check. (10min.)
        if (lazyCheck && lazy_check_threshold > (now - lastCheckTime)) {
            return;
        }
        CloseableHttpResponse response = null;
        try {
            response = myosotisHttpClient.execute(monitorRequest);
            if (response == null || response.getStatusLine() == null || response.getStatusLine().getStatusCode() != 200) {
                this.fail();
                return;
            }
            this.success();
        } catch (Exception e) {
            this.fail();
        } finally {
            this.reuse(response);
            lastCheckTime = now;
            if (!health) {
                logger.info("Cluster node[{}] health check {}.", address, "fail");
            }
        }
    }

    public void wakeUp(String namespace) {
        if (!health) {
            return;
        }
        HttpGet request = new HttpGet("http://" + address + polling_notify + namespace);
        request.addHeader(NetConst.client_host_ip, SystemConst.local_host);
        request.setConfig(NetConst.default_config);

        CloseableHttpResponse response = null;
        try {
            response = myosotisHttpClient.execute(request);
        } catch (Exception ignored) {
        } finally {
            this.reuse(response);
        }
    }

    private void success() {
        health = true;
        lazyCheck = false;
        failCount = 0;
    }

    private void fail() {
        health = false;
        failCount++;
        if (!lazyCheck && failCount >= 10) {
            lazyCheck = true;
        }
    }

    private void reuse(CloseableHttpResponse response) {
        if (response == null) {
            return;
        }
        try {
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getAddress() {
        return address;
    }

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public boolean isHealth() {
        return health;
    }

    public long getFailCount() {
        return failCount;
    }
}

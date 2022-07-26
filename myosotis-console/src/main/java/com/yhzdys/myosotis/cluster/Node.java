package com.yhzdys.myosotis.cluster;

import com.yhzdys.myosotis.constant.NetConst;
import com.yhzdys.myosotis.constant.SysConst;
import com.yhzdys.myosotis.misc.MyosotisHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Node {

    private static final Logger logger = LoggerFactory.getLogger(Node.class);

    private static final String health_check = "/cluster/health";
    private static final String polling_notify = "/cluster/polling/notify/";

    /**
     * lazy health check threshold (10min.)
     */
    private static final long lazy_check_threshold = TimeUnit.MINUTES.toMillis(10);

    private final MyosotisHttpClient myosotisHttpClient = MyosotisHttpClient.getInstance();

    private final String address;

    private final HttpGet monitorRequest;

    private long lastCheckTime;

    private boolean health;

    /**
     * client connections
     */
    private String state;

    private boolean lazyCheck;

    private long failCount;

    public Node(String address) {
        this.address = address;
        HttpGet request = new HttpGet("http://" + address + health_check);
        request.setHeader(NetConst.client_ip, SysConst.local_host);
        request.setHeader(NetConst.header_short_connection);
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
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                this.state = EntityUtils.toString(response.getEntity());
                this.success();
            } else {
                this.fail();
            }
        } catch (Exception e) {
            this.fail();
        } finally {
            this.consume(response);
            lastCheckTime = now;
            if (!health) {
                logger.info("Cluster node[{}] health check {}", address, "failed");
            }
        }
    }

    public void wakeUp(String namespace) {
        if (!health) {
            return;
        }
        HttpGet request = new HttpGet("http://" + address + polling_notify + namespace);
        request.setHeader(NetConst.client_ip, SysConst.local_host);
        request.setHeader(NetConst.header_short_connection);

        CloseableHttpResponse response = null;
        try {
            response = myosotisHttpClient.execute(request);
        } catch (Exception ignored) {
        } finally {
            this.consume(response);
        }
    }

    private void success() {
        health = true;
        lazyCheck = false;
        failCount = 0;
    }

    private void fail() {
        this.state = "unknown";
        health = false;
        failCount++;
        if (!lazyCheck && failCount >= 10) {
            lazyCheck = true;
        }
    }

    private void consume(CloseableHttpResponse response) {
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getFailCount() {
        return failCount;
    }
}

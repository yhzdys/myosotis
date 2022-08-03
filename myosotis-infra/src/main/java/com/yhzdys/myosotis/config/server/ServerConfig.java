package com.yhzdys.myosotis.config.server;

import com.yhzdys.myosotis.config.BaseConfig;

public class ServerConfig extends BaseConfig {

    private String logDir;

    private int port;
    private int minThreads;
    private int maxThreads;
    private int connectionTimeout;
    private int maxConnections;
    private int keepAliveRequests;
    private int acceptCount;

    private boolean enableCompress;
    private long compressThreshold;

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMinThreads() {
        return minThreads;
    }

    public void setMinThreads(int minThreads) {
        this.minThreads = minThreads;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getKeepAliveRequests() {
        return keepAliveRequests;
    }

    public void setKeepAliveRequests(int keepAliveRequests) {
        this.keepAliveRequests = keepAliveRequests;
    }

    public int getAcceptCount() {
        return acceptCount;
    }

    public void setAcceptCount(int acceptCount) {
        this.acceptCount = acceptCount;
    }

    public boolean isEnableCompress() {
        return enableCompress;
    }

    public void setEnableCompress(boolean enableCompress) {
        this.enableCompress = enableCompress;
    }

    public long getCompressThreshold() {
        return compressThreshold;
    }

    public void setCompressThreshold(long compressThreshold) {
        this.compressThreshold = compressThreshold;
    }
}

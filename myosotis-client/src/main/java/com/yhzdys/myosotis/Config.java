package com.yhzdys.myosotis;

import com.yhzdys.myosotis.enums.SerializeType;

/**
 * myosotis application config
 */
public class Config {

    private final String serverAddress;

    private SerializeType serializeType = SerializeType.JSON;

    /**
     * 开启本地快照
     */
    private boolean enableSnapshot = true;

    /**
     * 启用数据压缩
     */
    private boolean enableCompress = true;

    /**
     * 压缩阈值
     */
    private long compressThreshold = 2048L;

    public Config(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public SerializeType getSerializeType() {
        return serializeType;
    }

    public void serializeType(SerializeType serializeType) {
        this.serializeType = serializeType;
    }

    public boolean isEnableSnapshot() {
        return enableSnapshot;
    }

    public void enableSnapshot(boolean enableSnapshot) {
        this.enableSnapshot = enableSnapshot;
    }

    public boolean isEnableCompress() {
        return enableCompress;
    }

    public void enableCompress(boolean enableCompress) {
        this.enableCompress = enableCompress;
    }

    public long getCompressThreshold() {
        return compressThreshold;
    }

    public void compressThreshold(long compressThreshold) {
        this.compressThreshold = compressThreshold;
    }
}
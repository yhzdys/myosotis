package com.yhzdys.myosotis;

import com.yhzdys.myosotis.enums.SerializeType;

public class Config {

    private final String serverAddress;
    private SerializeType serializeType = SerializeType.JSON;
    private boolean enableSnapshot = true;
    private boolean enableCompress = true;
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

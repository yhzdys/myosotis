package com.yhzdys.myosotis;

import com.yhzdys.myosotis.enums.SerializeType;

public class MyosotisCustomizer {

    private String serverAddress;

    private SerializeType serializeType = SerializeType.JSON;

    /**
     * 开启本地文件配置，本地配置优先级高于远程配置
     */
    private boolean enableNative = true;

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

    public MyosotisCustomizer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public SerializeType getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(SerializeType serializeType) {
        this.serializeType = serializeType;
    }

    public boolean isEnableNative() {
        return enableNative;
    }

    public void setEnableNative(boolean enableNative) {
        this.enableNative = enableNative;
    }

    public boolean isEnableSnapshot() {
        return enableSnapshot;
    }

    public void setEnableSnapshot(boolean enableSnapshot) {
        this.enableSnapshot = enableSnapshot;
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

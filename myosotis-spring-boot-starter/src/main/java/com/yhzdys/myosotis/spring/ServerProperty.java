package com.yhzdys.myosotis.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "myosotis.server")
public class ServerProperty {

    private String address;
    private String serializeType;
    private Boolean enableSnapshot;
    private Boolean enableCompress;
    private Long compressThreshold;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(String serializeType) {
        this.serializeType = serializeType;
    }

    public Boolean getEnableSnapshot() {
        return enableSnapshot;
    }

    public void setEnableSnapshot(Boolean enableSnapshot) {
        this.enableSnapshot = enableSnapshot;
    }

    public Boolean getEnableCompress() {
        return enableCompress;
    }

    public void setEnableCompress(Boolean enableCompress) {
        this.enableCompress = enableCompress;
    }

    public Long getCompressThreshold() {
        return compressThreshold;
    }

    public void setCompressThreshold(Long compressThreshold) {
        this.compressThreshold = compressThreshold;
    }
}

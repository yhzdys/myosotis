package com.yhzdys.myosotis.spring;

import com.yhzdys.myosotis.exception.MyosotisException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "myosotis.server")
public class ServerProperty {

    private String address = "http://127.0.0.1:7777";

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (StringUtils.isEmpty(address)) {
            throw new MyosotisException("Server address may not be null");
        }
        this.address = address;
    }
}

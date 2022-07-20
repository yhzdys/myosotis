package com.yhzdys.myosotis.spring;

import com.yhzdys.myosotis.exception.MyosotisException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "myosotis.client")
public class ClientProperty {

    private String namespace = "default";

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        if (StringUtils.isEmpty(namespace)) {
            throw new MyosotisException("Client namespace may not be null");
        }
        this.namespace = namespace;
    }
}

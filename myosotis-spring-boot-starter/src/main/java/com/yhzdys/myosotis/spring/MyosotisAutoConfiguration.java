package com.yhzdys.myosotis.spring;

import com.yhzdys.myosotis.Config;
import com.yhzdys.myosotis.MyosotisApplication;
import com.yhzdys.myosotis.MyosotisClient;
import com.yhzdys.myosotis.enums.SerializeType;
import com.yhzdys.myosotis.exception.MyosotisException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ServerProperty.class, ClientProperty.class})
@ConditionalOnProperty(prefix = "myosotis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MyosotisAutoConfiguration {

    private ClientProperty clientProperties;

    private ServerProperty serverProperties;

    @Autowired
    public void setClientProperties(ClientProperty clientProperties) {
        this.clientProperties = clientProperties;
    }

    @Autowired
    public void setServerProperties(ServerProperty serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Bean
    @ConditionalOnMissingBean(MyosotisApplication.class)
    public MyosotisApplication myosotisApplication() {
        String address = serverProperties.getAddress();
        if (StringUtils.isBlank(address)) {
            throw new MyosotisException("Myosotis server address may not be null");
        }
        String serializeType = serverProperties.getSerializeType();
        Boolean enableSnapshot = serverProperties.getEnableSnapshot();
        Boolean enableCompress = serverProperties.getEnableCompress();
        Long compressThreshold = serverProperties.getCompressThreshold();
        Config config = new Config(address);
        if (StringUtils.isNotBlank(serializeType)) {
            config.serializeType(SerializeType.codeOf(serializeType));
        }
        if (enableSnapshot != null) {
            config.enableSnapshot(enableSnapshot);
        }
        if (enableCompress != null) {
            config.enableCompress(enableCompress);
        }
        if (compressThreshold != null && compressThreshold > 0L) {
            config.compressThreshold(compressThreshold);
        }
        return new MyosotisApplication(config);
    }

    @Bean
    public MyosotisClient myosotisClient(MyosotisApplication application) {
        String namespace = clientProperties.getNamespace();
        if (StringUtils.isBlank(namespace)) {
            throw new MyosotisException("Myosotis client namespace may not be null");
        }
        return application.getClient(namespace);
    }
}

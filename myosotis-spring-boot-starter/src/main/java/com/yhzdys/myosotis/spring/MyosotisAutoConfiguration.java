package com.yhzdys.myosotis.spring;

import com.yhzdys.myosotis.Config;
import com.yhzdys.myosotis.MyosotisApplication;
import com.yhzdys.myosotis.MyosotisClient;
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
        Config config = new Config(serverProperties.getAddress());
        return new MyosotisApplication(config);
    }

    @Bean
    public MyosotisClient myosotisClient(MyosotisApplication application) {
        return application.getClient(clientProperties.getNamespace());
    }
}

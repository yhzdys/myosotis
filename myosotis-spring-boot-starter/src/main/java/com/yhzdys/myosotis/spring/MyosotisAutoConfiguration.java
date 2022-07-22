package com.yhzdys.myosotis.spring;

import com.yhzdys.myosotis.MyosotisClient;
import com.yhzdys.myosotis.MyosotisClientManager;
import com.yhzdys.myosotis.MyosotisCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.yhzdys.myosotis.spring")
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
    @ConditionalOnMissingBean(MyosotisClientManager.class)
    public MyosotisClientManager myosotisClientManager() {
        MyosotisCustomizer customizer = new MyosotisCustomizer(serverProperties.getAddress());
        return new MyosotisClientManager(customizer);
    }

    @Bean
    public MyosotisClient myosotisClient(MyosotisClientManager clientManager) {
        return clientManager.getClient(clientProperties.getNamespace());
    }

    @Bean
    @ConditionalOnBean(MyosotisClientManager.class)
    public MyosotisValueAutoConfiguration myosotisValueAutoConfiguration () {
        return new MyosotisValueAutoConfiguration();
    }
}

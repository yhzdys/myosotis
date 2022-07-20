package com.yhzdys.myosotis.web;

import com.yhzdys.myosotis.config.server.ServerConfig;
import com.yhzdys.myosotis.config.server.ServerConfigLoader;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;

@Configuration
public class WebConfiguration implements WebMvcConfigurer, WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        ServerConfig config = ServerConfigLoader.get();

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        // 核心线程数
        threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        // 最大线程数
        threadPoolTaskExecutor.setMaxPoolSize(config.getKeepAliveRequests());
        // 配置队列大小
        threadPoolTaskExecutor.setQueueCapacity(Runtime.getRuntime().availableProcessors());
        // 配置线程前缀
        threadPoolTaskExecutor.setThreadNamePrefix("myosotis-polling-");
        threadPoolTaskExecutor.initialize();

        configurer.setDefaultTimeout(15 * 1000);
        configurer.setTaskExecutor(threadPoolTaskExecutor);
    }

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        ServerConfig config = ServerConfigLoader.get();

        factory.setPort(config.getPort());
        factory.setUriEncoding(StandardCharsets.UTF_8);
        factory.addConnectorCustomizers(
                connector -> {
                    Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                    protocol.setMinSpareThreads(config.getMinThreads());
                    protocol.setMaxThreads(config.getMaxThreads());
                    protocol.setAcceptCount(config.getAcceptCount());
                    protocol.setConnectionTimeout(config.getConnectionTimeout());
                    protocol.setMaxConnections(config.getMaxConnections());
                    protocol.setMaxKeepAliveRequests(config.getKeepAliveRequests());
                    // 30min.
                    protocol.setKeepAliveTimeout(30 * 60 * 1000);
                }
        );
    }
}

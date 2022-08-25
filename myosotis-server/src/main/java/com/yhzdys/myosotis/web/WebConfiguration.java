package com.yhzdys.myosotis.web;

import com.yhzdys.myosotis.config.server.ServerConfig;
import com.yhzdys.myosotis.config.server.ServerConfigLoader;
import com.yhzdys.myosotis.constant.SysConst;
import org.apache.coyote.http11.Http11NioProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class WebConfiguration implements WebMvcConfigurer, WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private ThreadPoolTaskExecutor asyncPollingPool;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        ServerConfig config = ServerConfigLoader.get();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(SysConst.processors);
        executor.setQueueCapacity(0);
        executor.setMaxPoolSize(config.getKeepAliveRequests());
        executor.setThreadNamePrefix("Myosotis-Polling-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();

        configurer.setDefaultTimeout(15 * 1000);
        configurer.setTaskExecutor(executor);
        asyncPollingPool = executor;
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
                    // 30sec.
                    protocol.setUseKeepAliveResponseHeader(true);
                    protocol.setKeepAliveTimeout(30 * 1000);
                }
        );
    }

    public int getConnections() {
        return asyncPollingPool.getActiveCount();
    }

    @ControllerAdvice
    public static class GlobalExceptionHandler {
        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ResponseBody
        @ExceptionHandler(AsyncRequestTimeoutException.class)
        public String handleAsyncRequestTimeoutException(HttpServletResponse response) {
            logger.error("Async polling timeout, too many client connections");
            response.setStatus(503);
            return "Async polling timeout, too many client connections";
        }

        @ResponseBody
        @ExceptionHandler(RejectedExecutionException.class)
        public String handleRejectedExecutionException(HttpServletResponse response) {
            ServerConfig config = ServerConfigLoader.get();
            String message = "Too many client connections(threshold: " + config.getKeepAliveRequests() + "), please check \"myosotis.server.keepAliveRequests\" in server.conf";
            logger.error(message);
            response.setStatus(503);
            return message;
        }

        @ResponseBody
        @ExceptionHandler(Exception.class)
        public String handleException(Exception e, HttpServletResponse response) {
            logger.error(e.getMessage(), e);
            response.setStatus(500);
            return "Unexpected Error: " + e.getMessage();
        }
    }
}
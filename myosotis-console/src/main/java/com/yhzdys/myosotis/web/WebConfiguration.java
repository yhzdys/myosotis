package com.yhzdys.myosotis.web;

import com.yhzdys.myosotis.config.console.ConsoleConfig;
import com.yhzdys.myosotis.config.console.ConsoleConfigLoader;
import com.yhzdys.myosotis.misc.BizException;
import com.yhzdys.myosotis.web.entity.WebResponse;
import com.yhzdys.myosotis.web.interceptor.LogInterceptor;
import com.yhzdys.myosotis.web.interceptor.PermissionInterceptor;
import com.yhzdys.myosotis.web.interceptor.SessionInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Configuration
public class WebConfiguration implements WebMvcConfigurer, WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Resource
    private SessionInterceptor sessionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/console/**", "/session/key", "/session/login", "/session/logout");
        registry.addInterceptor(new PermissionInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/console/**", "/session/**");
        registry.addInterceptor(new LogInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/console/**");
    }

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        ConsoleConfig config = ConsoleConfigLoader.get();
        factory.setPort(config.getPort());
        factory.setUriEncoding(StandardCharsets.UTF_8);
    }

    @ControllerAdvice
    public static class GlobalExceptionHandler {
        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(BizException.class)
        public WebResponse bizException(BizException e) {
            return WebResponse.fail(e.getMessage());
        }

        @ExceptionHandler(Exception.class)
        public WebResponse exception(Exception e) {
            logger.error(e.getMessage(), e);
            return WebResponse.fail(e.getMessage());
        }
    }
}

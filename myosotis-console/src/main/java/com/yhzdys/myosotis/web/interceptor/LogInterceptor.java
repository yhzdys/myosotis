package com.yhzdys.myosotis.web.interceptor;

import com.yhzdys.myosotis.misc.Const;
import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.web.entity.SessionContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class LogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String username = SessionContextHolder.getUsername();
        String uri = request.getRequestURI();
        Map<String, String[]> parameters = request.getParameterMap();
        logger.info(String.format(Const.log_template, username == null ? "?" : username, uri, JsonUtil.toString(parameters)));
        return true;
    }
}

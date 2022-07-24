package com.yhzdys.myosotis.web.interceptor;

import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.service.domain.Menu;
import com.yhzdys.myosotis.service.domain.Permission;
import com.yhzdys.myosotis.web.entity.SessionContextHolder;
import com.yhzdys.myosotis.web.entity.WebResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Permission permission = handlerMethod.getMethodAnnotation(Permission.class);
        if (permission == null || permission.value() == null) {
            return true;
        }
        List<Menu> menus = SessionContextHolder.getUserMenus();
        if (menus == null || !menus.contains(permission.value())) {
            try {
                response.getWriter().write(JsonUtil.toString(WebResponse.fail("How dare you!")));
            } catch (Exception ignored) {
            }
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }
}

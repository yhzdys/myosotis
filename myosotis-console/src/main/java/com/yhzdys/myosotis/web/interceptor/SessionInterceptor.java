package com.yhzdys.myosotis.web.interceptor;

import com.yhzdys.myosotis.database.object.MyosotisUserDO;
import com.yhzdys.myosotis.misc.Const;
import com.yhzdys.myosotis.service.SessionService;
import com.yhzdys.myosotis.service.UserService;
import com.yhzdys.myosotis.service.domain.SessionContext;
import com.yhzdys.myosotis.service.domain.UserRole;
import com.yhzdys.myosotis.web.entity.SessionContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Resource
    private SessionService sessionService;
    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return this.defaultVerify(request, response);
    }

    private boolean defaultVerify(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.setStatus(401);
            return false;
        }
        String sessionKey = null;
        for (Cookie cookie : cookies) {
            if (Const.session_key.equalsIgnoreCase(cookie.getName())) {
                sessionKey = cookie.getValue();
            }
        }
        if (StringUtils.isEmpty(sessionKey)) {
            response.setStatus(401);
            return false;
        }
        String username = sessionService.checkSessionKey(sessionKey);
        if (username == null) {
            response.setStatus(401);
            return false;
        }
        MyosotisUserDO user = userService.getByUsername(username);
        UserRole userRole = UserRole.codeOf(user.getUserRole());

        SessionContext context = new SessionContext();
        context.setUserId(user.getId());
        context.setUsername(username);
        context.setSessionKey(sessionKey);
        context.setUserRole(userRole);
        context.setMenus(userRole.menus());
        SessionContextHolder.save(context);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SessionContextHolder.clear();
    }
}

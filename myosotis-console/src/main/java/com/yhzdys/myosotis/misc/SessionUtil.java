package com.yhzdys.myosotis.misc;

import com.yhzdys.myosotis.service.domain.UserSession;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.UUID;

public class SessionUtil {
    private static final String random_chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String genSalt() {
        return RandomStringUtils.random(8, random_chars);
    }

    public static Date getLoginExpireDate() {
        return new Date(System.currentTimeMillis() + Const.login_timout);
    }

    public static Date getSessionExpireDate() {
        return new Date(System.currentTimeMillis() + Const.session_timout);
    }

    public static UserSession genSession() {
        UserSession session = new UserSession();
        session.setSessionKey(genSessionKey());
        session.setExpireTime(getSessionExpireDate());
        return session;
    }

    public static void setSession(UserSession session) {
        if (session == null) {
            return;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletResponse response = attributes.getResponse();
        if (response == null) {
            return;
        }

        long expireMillis = session.getExpireTime().getTime() - System.currentTimeMillis();
        int maxAge = (int) (expireMillis / 1000);

        Cookie sessionCookie = new Cookie(Const.session_key, session.getSessionKey());
        sessionCookie.setHttpOnly(true);
        sessionCookie.setMaxAge(maxAge);
        sessionCookie.setPath("/");

        response.addCookie(sessionCookie);
    }

    private static String genSessionKey() {
        return UUID.randomUUID().toString().toLowerCase();
    }

    public static void clearSession() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletResponse response = attributes.getResponse();
        if (response == null) {
            return;
        }
        Cookie sessionCookie = new Cookie(Const.session_key, "");
        sessionCookie.setHttpOnly(true);
        sessionCookie.setMaxAge(0);
        sessionCookie.setPath("/");

        response.addCookie(sessionCookie);
    }
}

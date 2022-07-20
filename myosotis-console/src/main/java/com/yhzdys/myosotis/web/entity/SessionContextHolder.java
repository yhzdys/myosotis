package com.yhzdys.myosotis.web.entity;

import com.yhzdys.myosotis.service.domain.Menu;
import com.yhzdys.myosotis.service.domain.SessionContext;
import com.yhzdys.myosotis.service.domain.UserRole;

import java.util.Collections;
import java.util.List;

public class SessionContextHolder {

    private static final ThreadLocal<SessionContext> holder = new ThreadLocal<>();

    public static void save(SessionContext context) {
        holder.set(context);
    }

    public static SessionContext get() {
        return holder.get();
    }

    public static String getUsername() {
        SessionContext context = holder.get();
        return context == null ? null : context.getUsername();
    }

    public static String getSessionKey() {
        SessionContext context = holder.get();
        return context == null ? null : context.getSessionKey();
    }

    public static UserRole getUserRole() {
        SessionContext context = holder.get();
        return context == null ? null : context.getUserRole();
    }

    public static List<Menu> getUserMenus() {
        SessionContext context = holder.get();
        return context == null ? Collections.emptyList() : context.getMenus();
    }

    public static void clear() {
        holder.remove();
    }
}

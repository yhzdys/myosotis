package com.yhzdys.myosotis.service.domain;

import com.yhzdys.myosotis.misc.BizException;

import java.util.Arrays;
import java.util.List;

public enum UserRole implements PairEnum {

    SUPERUSER("superuser", "超级用户") {
        @Override
        public List<Menu> menus() {
            return Arrays.asList(Menu.NAMESPACE, Menu.CONFIG, Menu.SERVER, Menu.USER);
        }
    },
    ADMIN("admin", "管理员") {
        @Override
        public List<Menu> menus() {
            return Arrays.asList(Menu.NAMESPACE, Menu.CONFIG, Menu.SERVER);
        }
    },
    DEVELOPER("developer", "开发者") {
        @Override
        public List<Menu> menus() {
            return Arrays.asList(Menu.NAMESPACE, Menu.CONFIG);
        }
    },
    ;

    private final String code;
    private final String name;

    UserRole(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static UserRole codeOf(String code) {
        for (UserRole userRole : values()) {
            if (userRole.getCode().equalsIgnoreCase(code)) {
                return userRole;
            }
        }
        throw new BizException("未知的用户角色");
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract List<Menu> menus();
}

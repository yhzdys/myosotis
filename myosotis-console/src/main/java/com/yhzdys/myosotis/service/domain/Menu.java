package com.yhzdys.myosotis.service.domain;

public enum Menu implements PairEnum {

    NAMESPACE("namespace_manage", "命名空间"),
    CONFIG("config_manage", "配置管理"),
    SERVER("server_manage", "服务监测"),
    USER("user_manage", "用户管理"),
    ;

    private final String code;
    private final String name;

    Menu(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}

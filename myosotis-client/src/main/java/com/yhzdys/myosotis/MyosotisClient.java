package com.yhzdys.myosotis;

/**
 * facade of myosotis configs
 */
public final class MyosotisClient {

    private final String namespace;

    private final MyosotisClientManager clientManager;

    MyosotisClient(String namespace, MyosotisClientManager clientManager) {
        this.namespace = namespace;
        this.clientManager = clientManager;
    }

    public String getConfig(String configKey) {
        return clientManager.getConfig(namespace, configKey);
    }

    public String getConfig(String configKey, String defaultValue) {
        String configValue = clientManager.getConfig(namespace, configKey);
        return configValue == null ? defaultValue : configValue;
    }

    public String getNamespace() {
        return namespace;
    }
}
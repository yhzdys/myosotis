package com.yhzdys.myosotis.config;

public class BaseConfig {

    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void initialized() {
        this.initialized = true;
    }
}

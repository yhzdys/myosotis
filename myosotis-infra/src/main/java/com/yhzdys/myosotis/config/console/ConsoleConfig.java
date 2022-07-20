package com.yhzdys.myosotis.config.console;

import com.yhzdys.myosotis.config.BaseConfig;

public class ConsoleConfig extends BaseConfig {

    private String logDir;

    private int port;

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

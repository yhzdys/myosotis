package com.yhzdys.myosotis.config.server;

import com.yhzdys.myosotis.InfraConst;
import com.yhzdys.myosotis.config.ConfigLoader;
import com.yhzdys.myosotis.exception.MyosotisException;

import java.util.Map;

public class ServerConfigLoader {

    private static final ServerConfig config = new ServerConfig();

    public static ServerConfig get() {
        load();
        return config;
    }

    private static void load() {
        if (config.isInitialized()) {
            return;
        }
        synchronized (config) {
            if (config.isInitialized()) {
                return;
            }
            initConfig();
        }
    }

    private static void initConfig() {
        Map<String, String> configs;
        try {
            configs = ConfigLoader.load(InfraConst.server_config_path);
        } catch (MyosotisException e) {
            throw e;
        } catch (Exception e) {
            throw new MyosotisException("Load config failed.", e);
        }

        config.setLogDir(configs.getOrDefault("myosotis.log.dir", InfraConst.default_log_dir));
        config.setPort(Integer.parseInt(configs.getOrDefault("myosotis.server.port", "7777")));
        config.setMinThreads(Integer.parseInt(configs.getOrDefault("myosotis.server.minThreads", "" + Runtime.getRuntime().availableProcessors())));
        config.setMaxThreads(Integer.parseInt(configs.getOrDefault("myosotis.server.maxThreads", "512")));
        config.setConnectionTimeout(Integer.parseInt(configs.getOrDefault("myosotis.server.connectionTimeout", "2000")));
        config.setMaxConnections(Integer.parseInt(configs.getOrDefault("myosotis.server.maxConnections", "1024")));
        config.setKeepAliveRequests(Integer.parseInt(configs.getOrDefault("myosotis.server.keepAliveRequests", "128")));
        config.setAcceptCount(Integer.parseInt(configs.getOrDefault("myosotis.server.acceptCount", "8")));
        config.setEnableCompress(Boolean.parseBoolean(configs.getOrDefault("myosotis.server.enableCompress", "true")));
        config.setCompressThreshold(Long.parseLong(configs.getOrDefault("myosotis.server.compressThreshold", "2048")));

        config.initialized();
    }
}

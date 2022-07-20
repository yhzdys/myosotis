package com.yhzdys.myosotis.config.console;

import com.yhzdys.myosotis.InfraConst;
import com.yhzdys.myosotis.config.ConfigLoader;
import com.yhzdys.myosotis.constant.SystemConst;
import com.yhzdys.myosotis.exception.MyosotisException;

import java.util.Map;

public class ConsoleConfigLoader {

    private static final ConsoleConfig config = new ConsoleConfig();

    public static ConsoleConfig get() {
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
            configs = ConfigLoader.load(InfraConst.console_config_path);
        } catch (MyosotisException e) {
            throw e;
        } catch (Exception e) {
            throw new MyosotisException("Load config failed.", e);
        }
        String logDir = configs.get("myosotis.log.dir");
        if (logDir == null) {
            config.setLogDir(InfraConst.default_log_dir);
        } else {
            config.setLogDir(logDir.endsWith(SystemConst.separator) ? logDir.substring(0, logDir.length() - 1) : logDir);
        }
        config.setPort(Integer.parseInt(configs.getOrDefault("myosotis.console.port", "7776")));

        config.initialized();
    }
}

package com.yhzdys.myosotis.config.datasource;

import com.yhzdys.myosotis.InfraConst;
import com.yhzdys.myosotis.config.ConfigLoader;
import com.yhzdys.myosotis.exception.MyosotisException;

import java.util.Map;

public class DatasourceConfigLoader {

    private static final DatasourceConfig config = new DatasourceConfig();

    public static DatasourceConfig get() {
        load();
        return config;
    }

    public static void load() {
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
            configs = ConfigLoader.load(InfraConst.datasource_config_path);
        } catch (MyosotisException e) {
            throw e;
        } catch (Exception e) {
            throw new MyosotisException("Load datasource config failed.", e);
        }
        config.setMysqlUrl(configs.get("myosotis.mysql.url"));
        config.setMysqlUsername(configs.get("myosotis.mysql.username"));
        config.setMysqlPassword(configs.get("myosotis.mysql.password"));

        config.initialized();
    }

}

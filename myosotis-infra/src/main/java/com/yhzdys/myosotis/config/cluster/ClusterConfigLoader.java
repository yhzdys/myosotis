package com.yhzdys.myosotis.config.cluster;

import com.yhzdys.myosotis.InfraConst;
import com.yhzdys.myosotis.exception.MyosotisException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ClusterConfigLoader {

    private static final ClusterConfig config = new ClusterConfig();

    public static ClusterConfig get() {
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

    public static void reload() {
        synchronized (config) {
            initConfig();
        }
    }

    private static void initConfig() {
        String filePath = InfraConst.cluster_config_path;
        try {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(filePath));
            } catch (Exception e) {
                throw new MyosotisException("Can not find config file: " + filePath);
            }
            List<String> nodes = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.startsWith("#") || line.startsWith("/")) {
                    continue;
                }
                nodes.add(line);
            }
            config.setClusterNodes(nodes);
        } catch (MyosotisException e) {
            throw e;
        } catch (Exception e) {
            throw new MyosotisException("Load cluster config failed.", e);
        }

        config.initialized();
    }
}

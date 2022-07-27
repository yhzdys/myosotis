package com.yhzdys.myosotis.config;

import com.yhzdys.myosotis.exception.MyosotisException;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {

    public static Map<String, String> load(String filePath) throws Exception {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (Exception e) {
            throw new MyosotisException("Can not find config file: " + filePath);
        }
        Map<String, String> configMap = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            Config config = readConfig(line.trim());
            if (config == null) {
                continue;
            }
            configMap.put(config.getKey(), config.getValue());
        }
        return configMap;
    }

    private static Config readConfig(String line) {
        if (line.length() == 0 || line.startsWith("#") || line.startsWith("/")) {
            return null;
        }
        if (!line.contains("=") || line.startsWith("=") || line.endsWith("=")) {
            return null;
        }
        String[] split = line.split("=", 2);
        String configKey = split[0].trim();
        String configValue = split[1].trim();
        if (StringUtils.isAnyEmpty(configKey, configValue)) {
            throw new MyosotisException("Invalid config: " + line);
        }
        return new Config(configKey, configValue);
    }

    private static final class Config {
        private final String key;
        private final String value;

        public Config(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}

package com.yhzdys.myosotis.processor;

import com.yhzdys.myosotis.constant.SystemConst;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.enums.EventType;
import com.yhzdys.myosotis.util.FileUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * read local config file and fetch config change events
 *
 * @see com.yhzdys.myosotis.processor.Processor
 */
public final class LocalProcessor implements Processor {

    private final String config_dir = SystemConst.myosotis_dir + SystemConst.separator + "%s";
    private final String config_file = config_dir + SystemConst.separator + "%s";

    /**
     * 每个key的本地配置文件最近修改时间
     * <namespace, <configKey, timestamp>>
     */
    private final ConcurrentMap<String, ConcurrentMap<String, Long>> fileModifiedMap = new ConcurrentHashMap<>(2);

    @Override
    public void init(String namespace) {
        // 初始化配置缓存
        fileModifiedMap.put(namespace, new ConcurrentHashMap<>(0));
        // 创建文件目录
        File configDir = new File(String.format(config_dir, namespace));
        boolean result = configDir.mkdirs();
    }

    /**
     * 获取本地配置文件变更事件
     */
    @Override
    public List<MyosotisEvent> fetchEvents(Map<String, String> cachedConfigs, String namespace) {
        List<MyosotisEvent> events = new ArrayList<>();
        // 关注的本地key文件最新一次修改时间
        Map<String, Long> localFileLastModified = getLocalFileLastModified(namespace, cachedConfigs);
        // 本地key文件缓存的上次读取的修改时间
        ConcurrentMap<String, Long> cachedLastModified = fileModifiedMap.get(namespace);

        for (Map.Entry<String, Long> entry : cachedLastModified.entrySet()) {
            String configKey = entry.getKey();
            Long cachedModified = entry.getValue();

            Long realModified = localFileLastModified.get(configKey);
            // 剩下的就是新增事件.
            localFileLastModified.remove(configKey);
            // delete event
            if (realModified == null) {
                cachedLastModified.remove(configKey);
                events.add(new MyosotisEvent(namespace, configKey, EventType.DELETE));
                continue;
            }
            // no changes
            if (Objects.equals(cachedModified, realModified)) {
                continue;
            }
            // update event
            cachedLastModified.put(configKey, realModified);
            String configValue = getConfigValue(namespace, configKey);
            // no changes
            if (StringUtils.isEmpty(configValue) || configValue.equals(cachedConfigs.get(configKey))) {
                continue;
            }
            events.add(new MyosotisEvent(namespace, configKey, configValue, EventType.UPDATE));
        }
        // add event
        for (Map.Entry<String, Long> entry : localFileLastModified.entrySet()) {
            cachedLastModified.put(entry.getKey(), entry.getValue());
            String configValue = this.getConfigValue(namespace, entry.getKey());
            if (StringUtils.isEmpty(configValue)) {
                continue;
            }
            events.add(new MyosotisEvent(namespace, entry.getKey(), configValue, EventType.ADD));
        }
        return events;
    }

    @Override
    public List<MyosotisEvent> pollingEvents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MyosotisConfig getConfig(String namespace, String configKey) {
        File configFile = getConfigFile(namespace, configKey);
        String configValue = null;
        if (configFile.exists() && configFile.isFile()) {
            // 记录当前key的最后修改时间
            fileModifiedMap.get(namespace).put(configKey, configFile.lastModified());
            configValue = FileUtil.read(configFile);
        }
        if (StringUtils.isEmpty(configValue)) {
            return null;
        }
        return new MyosotisConfig(namespace, configKey, configValue);
    }

    @Override
    public List<MyosotisConfig> getConfigs(String namespace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MyosotisConfig> getConfigs(Map<String, Map<String, Long>> namespaceKeyMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(MyosotisConfig config) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取本地配置文件的最后修改时间
     */
    private Map<String, Long> getLocalFileLastModified(String namespace, Map<String, String> configs) {
        File configDir = new File(MessageFormat.format(config_dir, namespace));
        if (!configDir.exists() || !configDir.isDirectory()) {
            return Collections.emptyMap();
        }
        File[] files = configDir.listFiles();
        if (files == null || files.length < 1) {
            return Collections.emptyMap();
        }
        Map<String, Long> localFileLastModified = new HashMap<>(files.length);
        for (File file : files) {
            if (configs.containsKey(file.getName())) {
                localFileLastModified.put(file.getName(), file.lastModified());
            }
        }
        return localFileLastModified;
    }

    private String getConfigValue(String namespace, String configKey) {
        File configFile = getConfigFile(namespace, configKey);
        return getConfigValue(configFile);
    }

    private File getConfigFile(String namespace, String configKey) {
        return new File(String.format(config_file, namespace, configKey));
    }

    private String getConfigValue(File configFile) {
        if (configFile.exists() && configFile.isFile()) {
            return FileUtil.read(configFile);
        }
        return null;
    }
}

package com.yhzdys.myosotis.processor;

import com.yhzdys.myosotis.constant.SystemConst;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.util.FileUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * the snapshot of configs from local snapshot file
 *
 * @see com.yhzdys.myosotis.processor.Processor
 */
public final class SnapshotProcessor implements Processor {

    public static final String sn_dir = SystemConst.myosotis_dir + SystemConst.separator + "snapshot" + SystemConst.separator + "%s";
    public static final String sn_file = sn_dir + SystemConst.separator + "%s" + ".snapshot";

    @Override
    public void init(String namespace) {
        // init dir of snapshot file
        File dir = new File(String.format(sn_dir, namespace));
        boolean result = dir.mkdirs();
    }

    @Override
    public List<MyosotisEvent> fetchEvents(Map<String, String> cachedConfigs, String namespace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MyosotisEvent> pollingEvents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MyosotisConfig getConfig(String namespace, String configKey) {
        File file = new File(String.format(sn_file, namespace, configKey));
        if (!file.exists()) {
            return null;
        }
        try {
            return JsonUtil.toObject(FileUtil.read(file), MyosotisConfig.class);
        } catch (Exception e) {
            return null;
        }
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
    public void save(MyosotisConfig data) {
        FileUtil.save(JsonUtil.toString(data), String.format(sn_file, data.getNamespace(), data.getConfigKey()));
    }
}

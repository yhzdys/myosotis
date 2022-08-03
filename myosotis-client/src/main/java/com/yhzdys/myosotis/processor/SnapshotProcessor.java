package com.yhzdys.myosotis.processor;

import com.yhzdys.myosotis.constant.SystemConst;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.misc.FileTool;
import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.misc.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class SnapshotProcessor implements Processor {

    public static final String sn_dir = SystemConst.myosotis_dir + SystemConst.separator + "snapshot" + SystemConst.separator + "%s";
    public static final String sn_file = sn_dir + SystemConst.separator + "%s" + ".snapshot";

    private final boolean enable;

    public SnapshotProcessor(boolean enable) {
        this.enable = enable;
    }

    @Override
    public void init(String namespace) {
        if (!enable) {
            return;
        }
        File dir = new File(String.format(sn_dir, namespace));
        boolean result = dir.mkdirs();
    }

    @Override
    public List<MyosotisEvent> fetchEvents() {
        return Collections.emptyList();
    }

    @Override
    public MyosotisConfig getConfig(String namespace, String configKey) {
        if (!enable) {
            return null;
        }
        File file = new File(String.format(sn_file, namespace, configKey));
        if (!file.exists()) {
            return null;
        }
        try {
            return JsonUtil.toObject(FileTool.read(file), MyosotisConfig.class);
        } catch (Exception e) {
            LoggerFactory.getLogger().error("Parse snapshot config error", e);
            return null;
        }
    }

    @Override
    public List<MyosotisConfig> getConfigs(String namespace) {
        return Collections.emptyList();
    }

    @Override
    public void save(MyosotisConfig data) {
        if (!enable) {
            return;
        }
        FileTool.save(JsonUtil.toString(data), String.format(sn_file, data.getNamespace(), data.getConfigKey()));
    }
}

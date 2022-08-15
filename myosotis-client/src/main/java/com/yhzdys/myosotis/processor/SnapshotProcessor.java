package com.yhzdys.myosotis.processor;

import com.yhzdys.myosotis.constant.SystemConst;
import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.misc.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
            return JsonUtil.toObject(this.readFile(file), MyosotisConfig.class);
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
        this.saveFile(JsonUtil.toString(data), String.format(sn_file, data.getNamespace(), data.getConfigKey()));
    }

    private String readFile(File file) {
        try (FileInputStream fis = new FileInputStream(file); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[128];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }
            return bos.toString("UTF-8").trim();
        } catch (Exception e) {
            LoggerFactory.getLogger().error("Read file failed", e);
            return null;
        }
    }

    private void saveFile(String data, String path) {
        File file = new File(path);
        Writer writer = null;
        try {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                LoggerFactory.getLogger().warn("Mkdir snapshot parent file failed. path: {}", path);
                return;
            }
            if (!file.exists() && !file.createNewFile()) {
                LoggerFactory.getLogger().warn("Create snapshot file failed. path: {}", path);
                return;
            }
            writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
            writer.write(data.trim());
        } catch (Exception e) {
            LoggerFactory.getLogger().error("Save file failed", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}

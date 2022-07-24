package com.yhzdys.myosotis.misc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class FileTool {

    public static String read(File file) {
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

    public static void save(String data, String path) {
        File file = new File(path);
        Writer writer = null;
        try {
            if (file.exists() && !file.delete()) {
                return;
            }
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                return;
            }
            if (!file.createNewFile()) {
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
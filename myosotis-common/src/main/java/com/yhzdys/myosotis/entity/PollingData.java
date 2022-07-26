package com.yhzdys.myosotis.entity;

import java.util.Map;

/**
 * long polling data
 */
public final class PollingData {

    private boolean isAll;

    private String namespace;

    /**
     * <configKey, version>
     */
    private Map<String, Integer> data;

    public PollingData() {
    }

    public PollingData(boolean isAll, String namespace, Map<String, Integer> data) {
        this.isAll = isAll;
        this.namespace = namespace;
        this.data = data;
    }

    public boolean isAll() {
        return isAll;
    }

    public void setAll(boolean isAll) {
        this.isAll = isAll;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Map<String, Integer> getData() {
        return data;
    }

    public void setData(Map<String, Integer> data) {
        this.data = data;
    }
}

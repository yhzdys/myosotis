package com.yhzdys.myosotis.config.cluster;

import com.yhzdys.myosotis.config.BaseConfig;

import java.util.List;

public class ClusterConfig extends BaseConfig {

    private List<String> clusterNodes;

    public List<String> getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(List<String> clusterNodes) {
        this.clusterNodes = clusterNodes;
    }
}

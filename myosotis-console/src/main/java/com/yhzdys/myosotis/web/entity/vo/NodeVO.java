package com.yhzdys.myosotis.web.entity.vo;

import com.yhzdys.myosotis.InfraConst;
import com.yhzdys.myosotis.cluster.Node;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NodeVO {

    private String address;
    private String lastCheckTime;
    private Long failCount;
    private String state;
    private Boolean health;

    public NodeVO convert(Node node) {
        this.address = node.getAddress();
        this.lastCheckTime = node.getLastCheckTime() <= 0L ? StringUtils.EMPTY :
                new SimpleDateFormat(InfraConst.default_time_format).format(new Date(node.getLastCheckTime()));
        this.failCount = node.getFailCount();
        this.state = node.getState();
        this.health = node.isHealth();
        return this;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(String lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public Long getFailCount() {
        return failCount;
    }

    public void setFailCount(Long failCount) {
        this.failCount = failCount;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean getHealth() {
        return health;
    }

    public void setHealth(Boolean health) {
        this.health = health;
    }
}

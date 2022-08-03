package com.yhzdys.myosotis.database.object;

import java.util.Date;

public class MyosotisAuthorityDO {

    private Long id;
    private String username;
    private String namespace;
    private Date createTime;
    private Date updateTime;

    public MyosotisAuthorityDO() {
    }

    public MyosotisAuthorityDO(String username, String namespace) {
        this.username = username;
        this.namespace = namespace;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}

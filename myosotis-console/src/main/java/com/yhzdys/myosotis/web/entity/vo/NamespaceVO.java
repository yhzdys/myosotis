package com.yhzdys.myosotis.web.entity.vo;

import com.yhzdys.myosotis.database.object.MyosotisNamespaceDO;

public class NamespaceVO {

    private Long id;

    private String namespace;

    private String description;

    private String owners;

    private Long configCount;

    public NamespaceVO convert(MyosotisNamespaceDO namespace) {
        this.id = namespace.getId();
        this.namespace = namespace.getNamespace();
        this.description = namespace.getDescription();
        this.configCount = 0L;
        return this;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwners() {
        return owners;
    }

    public void setOwners(String owners) {
        this.owners = owners;
    }

    public Long getConfigCount() {
        return configCount;
    }

    public void setConfigCount(Long configCount) {
        this.configCount = configCount;
    }
}

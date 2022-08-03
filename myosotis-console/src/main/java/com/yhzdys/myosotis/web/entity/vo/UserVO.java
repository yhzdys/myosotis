package com.yhzdys.myosotis.web.entity.vo;

import com.yhzdys.myosotis.InfraConst;
import com.yhzdys.myosotis.database.object.MyosotisUserDO;
import com.yhzdys.myosotis.service.domain.UserRole;

import java.text.SimpleDateFormat;

public class UserVO {

    private Long id;
    private String username;
    private PairVO userRole;
    private String namespaces;
    private String createTime;

    public UserVO convert(MyosotisUserDO user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.userRole = UserRole.codeOf(user.getUserRole()).toPair();
        this.createTime = new SimpleDateFormat(InfraConst.default_time_format).format(user.getCreateTime());
        return this;
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

    public PairVO getUserRole() {
        return userRole;
    }

    public void setUserRole(PairVO userRole) {
        this.userRole = userRole;
    }

    public String getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(String namespaces) {
        this.namespaces = namespaces;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}

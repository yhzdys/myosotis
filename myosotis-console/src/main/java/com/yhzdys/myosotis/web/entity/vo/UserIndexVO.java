package com.yhzdys.myosotis.web.entity.vo;

import java.util.List;

public class UserIndexVO {

    private Long userId;

    private String username;

    private PairVO userRole;

    private List<PairVO> menus;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public List<PairVO> getMenus() {
        return menus;
    }

    public void setMenus(List<PairVO> menus) {
        this.menus = menus;
    }
}

package com.yhzdys.myosotis.service.domain;

import com.yhzdys.myosotis.web.entity.vo.UserVO;

import java.util.Date;

public class UserSession {

    private String sessionKey;

    private Date expireTime;

    private UserVO user;

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}

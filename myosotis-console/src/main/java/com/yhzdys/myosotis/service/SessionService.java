package com.yhzdys.myosotis.service;

import com.yhzdys.myosotis.database.mapper.MyosotisSessionMapper;
import com.yhzdys.myosotis.database.mapper.MyosotisUserMapper;
import com.yhzdys.myosotis.database.object.MyosotisSessionDO;
import com.yhzdys.myosotis.database.object.MyosotisUserDO;
import com.yhzdys.myosotis.misc.BizException;
import com.yhzdys.myosotis.misc.Encryptor;
import com.yhzdys.myosotis.misc.SessionUtil;
import com.yhzdys.myosotis.service.domain.UserSession;
import com.yhzdys.myosotis.web.entity.SessionContextHolder;
import com.yhzdys.myosotis.web.entity.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

@Service
public class SessionService {

    @Resource
    private MyosotisSessionMapper sessionMapper;
    @Resource
    private MyosotisUserMapper userMapper;

    public String checkSessionKey(String sessionKey) {
        MyosotisSessionDO session = sessionMapper.selectBySessionKey(sessionKey);
        if (session == null) {
            return null;
        }
        if (session.getExpireTime().before(new Date())) {
            sessionMapper.deleteById(session.getId());
            return null;
        }
        return session.getUsername();
    }

    public String getLoginKey(String username) {
        MyosotisUserDO user = userMapper.selectByUsername(username);
        Pair<String, String> keyPair = Encryptor.genRsaKeyPair();
        if (user == null) {
            return keyPair.getLeft();
        }
        Date now = new Date();
        MyosotisSessionDO session = sessionMapper.selectByUsername(username);
        if (session != null) {
            MyosotisSessionDO update = new MyosotisSessionDO();
            update.setId(session.getId());
            update.setSessionKey(StringUtils.EMPTY);
            update.setPrivateKey(keyPair.getRight());
            update.setExpireTime(SessionUtil.getLoginExpireDate());
            update.setUpdateTime(now);
            sessionMapper.update(update);
        } else {
            session = new MyosotisSessionDO();
            session.setUsername(username);
            session.setPrivateKey(keyPair.getRight());
            session.setExpireTime(SessionUtil.getLoginExpireDate());
            session.setCreateTime(now);
            session.setUpdateTime(now);
            sessionMapper.insert(session);
        }
        return keyPair.getLeft();
    }

    public UserSession login(String username, String password) {
        MyosotisSessionDO session = this.checkSession(username);
        MyosotisUserDO user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        password = Encryptor.decryptByPrivateKey(password, session.getPrivateKey());
        String passwd = Encryptor.md5(password + user.getSalt());
        if (!Objects.equals(passwd, user.getPassword())) {
            throw new BizException("密码错误");
        }
        UserSession newSession = SessionUtil.genSession();
        newSession.setUser(new UserVO().convert(user));

        MyosotisSessionDO update = new MyosotisSessionDO();
        update.setId(session.getId());
        update.setSessionKey(newSession.getSessionKey());
        update.setUsername(username);
        update.setPrivateKey(StringUtils.EMPTY);
        update.setExpireTime(newSession.getExpireTime());
        update.setUpdateTime(new Date());
        sessionMapper.update(update);
        return newSession;
    }

    public void password(String username, String oldPassword, String password) {
        MyosotisSessionDO session = this.checkSession(username);
        MyosotisUserDO user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        oldPassword = Encryptor.decryptByPrivateKey(oldPassword, session.getPrivateKey());
        String oldPasswd = Encryptor.md5(oldPassword + user.getSalt());
        if (!Objects.equals(oldPasswd, user.getPassword())) {
            throw new BizException("原密码错误");
        }

        password = Encryptor.decryptByPrivateKey(password, session.getPrivateKey());
        String salt = SessionUtil.genSalt();
        MyosotisUserDO update = new MyosotisUserDO();
        update.setId(user.getId());
        update.setPassword(Encryptor.md5(password + salt));
        update.setSalt(salt);
        update.setUpdateTime(new Date());
        userMapper.update(update);
        sessionMapper.deleteById(session.getId());
    }

    public void logout() {
        MyosotisSessionDO session = sessionMapper.selectBySessionKey(
                SessionContextHolder.getSessionKey()
        );
        if (session == null) {
            return;
        }
        sessionMapper.deleteById(session.getId());
    }

    private MyosotisSessionDO checkSession(String username) {
        MyosotisSessionDO session = sessionMapper.selectByUsername(username);
        if (session == null || StringUtils.isEmpty(session.getPrivateKey())) {
            throw new BizException("会话已过期");
        }
        if (session.getExpireTime().before(new Date())) {
            sessionMapper.deleteById(session.getId());
            throw new BizException("会话已过期.");
        }
        return session;
    }
}

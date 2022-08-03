package com.yhzdys.myosotis.service;

import com.yhzdys.myosotis.database.mapper.MyosotisSessionMapper;
import com.yhzdys.myosotis.database.mapper.MyosotisUserMapper;
import com.yhzdys.myosotis.database.object.MyosotisAuthorityDO;
import com.yhzdys.myosotis.database.object.MyosotisUserDO;
import com.yhzdys.myosotis.misc.BizException;
import com.yhzdys.myosotis.misc.Const;
import com.yhzdys.myosotis.misc.Encryptor;
import com.yhzdys.myosotis.misc.SessionUtil;
import com.yhzdys.myosotis.web.entity.Page;
import com.yhzdys.myosotis.web.entity.UserRequest;
import com.yhzdys.myosotis.web.entity.vo.UserVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService extends AuthorityService {

    @Resource
    private MyosotisUserMapper userMapper;
    @Resource
    private MyosotisSessionMapper sessionMapper;

    public MyosotisUserDO getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public Page page(String keyword, Integer page) {
        Long count = userMapper.count(keyword);
        if (count == null || count < 1L) {
            return Page.empty();
        }
        List<MyosotisUserDO> users = userMapper.list(keyword, page * Const.page_size - Const.page_size, Const.page_size);
        if (CollectionUtils.isEmpty(users)) {
            return Page.empty();
        }
        List<UserVO> voList = new ArrayList<>(users.size());
        for (MyosotisUserDO user : users) {
            UserVO vo = new UserVO().convert(user);
            if (Const.default_username.equals(user.getUsername())) {
                vo.setNamespaces("ALL");
            } else {
                List<MyosotisAuthorityDO> list = authorityMapper.listByUsername(user.getUsername());
                vo.setNamespaces(list.stream().map(MyosotisAuthorityDO::getNamespace).collect(Collectors.joining(",")));
            }
            voList.add(vo);
        }
        return new Page(voList, page, count, (long) page * Const.page_size >= count);
    }

    public String resetPassword(Long id) {
        MyosotisUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        String salt = SessionUtil.genSalt();
        String password = Encryptor.md5(Encryptor.md5(Const.default_password) + salt);
        MyosotisUserDO update = new MyosotisUserDO();
        update.setId(user.getId());
        update.setPassword(password);
        update.setSalt(salt);
        update.setUpdateTime(new Date());
        userMapper.update(update);
        sessionMapper.deleteByUsername(user.getUsername());
        return user.getUsername();
    }

    public UserVO getById(Long id) {
        MyosotisUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (Const.default_username.equals(user.getUsername())) {
            throw new BizException("系统初始用户不允许修改");
        }
        UserVO vo = new UserVO().convert(user);
        List<MyosotisAuthorityDO> list = authorityMapper.listByUsername(user.getUsername());
        vo.setNamespaces(list.stream().map(MyosotisAuthorityDO::getNamespace).collect(Collectors.joining(",")));
        return vo;
    }

    public void add(UserRequest request) {
        MyosotisUserDO user = userMapper.selectByUsername(request.getUsername());
        if (user != null) {
            throw new BizException("用户已存在");
        }
        String salt = SessionUtil.genSalt();
        String password = Encryptor.md5(Encryptor.md5(Const.default_password) + salt);

        user = new MyosotisUserDO();
        user.setUsername(request.getUsername());
        user.setPassword(password);
        user.setSalt(salt);
        user.setUserRole(request.getUserRole());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        this.updateUserNamespaces(user.getUsername(), request.getNamespaces());
    }

    public void update(UserRequest request) {
        MyosotisUserDO user = userMapper.selectById(request.getId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (Const.default_username.equals(user.getUsername())) {
            throw new BizException("系统初始用户不允许修改");
        }
        MyosotisUserDO update = new MyosotisUserDO();
        update.setId(user.getId());
        update.setUserRole(request.getUserRole());
        update.setUpdateTime(new Date());
        userMapper.update(update);
        this.updateUserNamespaces(user.getUsername(), request.getNamespaces());
    }

    public void delete(Long id) {
        MyosotisUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (Const.default_username.equals(user.getUsername())) {
            throw new BizException("系统初始用户不允许删除");
        }
        userMapper.delete(id);
        sessionMapper.deleteByUsername(user.getUsername());
        authorityMapper.deleteByUsername(user.getUsername());
    }
}

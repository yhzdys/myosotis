package com.yhzdys.myosotis.service;

import com.yhzdys.myosotis.database.mapper.MyosotisConfigMapper;
import com.yhzdys.myosotis.database.mapper.MyosotisNamespaceMapper;
import com.yhzdys.myosotis.database.object.MyosotisAuthorityDO;
import com.yhzdys.myosotis.database.object.MyosotisNamespaceDO;
import com.yhzdys.myosotis.misc.BizException;
import com.yhzdys.myosotis.misc.Const;
import com.yhzdys.myosotis.service.domain.UserRole;
import com.yhzdys.myosotis.web.entity.EditRequest;
import com.yhzdys.myosotis.web.entity.Page;
import com.yhzdys.myosotis.web.entity.SessionContextHolder;
import com.yhzdys.myosotis.web.entity.vo.NamespaceVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NamespaceService extends AuthorityService {

    @Resource
    private MyosotisNamespaceMapper namespaceMapper;

    @Resource
    private MyosotisConfigMapper configMapper;

    public Page page(String keyword, Integer page) {
        List<String> namespaces = getUserNamespaces();
        if (namespaces != null && namespaces.size() < 1) {
            return Page.empty();
        }
        Long count = namespaceMapper.count(namespaces, keyword);
        if (count < 1) {
            return Page.empty();
        }
        List<MyosotisNamespaceDO> list = namespaceMapper.list(namespaces, keyword, page * Const.page_size - Const.page_size, Const.page_size);
        if (CollectionUtils.isEmpty(list)) {
            return Page.empty();
        }
        List<NamespaceVO> voList = new ArrayList<>(list.size());
        for (MyosotisNamespaceDO namespace : list) {
            NamespaceVO vo = new NamespaceVO().convert(namespace);
            Long configCount = configMapper.countByNamespace(namespace.getNamespace());
            vo.setConfigCount(configCount == null ? 0L : configCount);
            List<MyosotisAuthorityDO> authorities = authorityMapper.listByNamespace(namespace.getNamespace());
            vo.setOwners(
                    authorities.stream()
                            .map(MyosotisAuthorityDO::getUsername)
                            .collect(Collectors.joining(","))
            );
            voList.add(vo);
        }
        return new Page(voList, page, count, (long) page * Const.page_size >= count);
    }

    public NamespaceVO get(Long id) {
        MyosotisNamespaceDO namespace = namespaceMapper.selectById(id);
        if (namespace == null) {
            throw new BizException("命名空间不存在");
        }
        NamespaceVO vo = new NamespaceVO().convert(namespace);
        List<MyosotisAuthorityDO> authorities = authorityMapper.listByNamespace(namespace.getNamespace());
        vo.setOwners(
                authorities.stream()
                        .map(MyosotisAuthorityDO::getUsername)
                        .collect(Collectors.joining(","))
        );
        return vo;
    }

    public void delete(Long id) {
        MyosotisNamespaceDO namespace = namespaceMapper.selectById(id);
        if (namespace == null) {
            throw new BizException("命名空间不存在");
        }
        List<String> namespaces = getUserNamespaces();
        if (namespaces != null && !namespaces.contains(namespace.getNamespace())) {
            throw new BizException("没有权限");
        }
        Long configCount = configMapper.countByNamespace(namespace.getNamespace());
        if (configCount != null && configCount > 0) {
            throw new BizException("请清空配置后再删除命名空间");
        }
        namespaceMapper.delete(id);
        authorityMapper.deleteByNamespace(namespace.getNamespace());
    }

    public void update(EditRequest request) {
        MyosotisNamespaceDO namespace = namespaceMapper.selectById(request.getId());
        if (namespace == null) {
            throw new BizException("命名空间不存在");
        }
        List<String> namespaces = getUserNamespaces();
        if (namespaces != null && !namespaces.contains(namespace.getNamespace())) {
            throw new BizException("没有权限");
        }
        MyosotisNamespaceDO update = new MyosotisNamespaceDO();
        update.setId(namespace.getId());
        update.setDescription(request.getDescription());
        update.setUpdateTime(new Date());
        namespaceMapper.update(update);
        this.updateNamespaceUsers(namespace.getNamespace(), request.getOwners());
    }

    public void add(EditRequest request) {
        if (SessionContextHolder.getUserRole() != UserRole.SUPERUSER) {
            throw new BizException("没有权限");
        }
        if (StringUtils.isEmpty(request.getName())) {
            throw new BizException("命名空间名称不能为空");
        }
        MyosotisNamespaceDO namespace = namespaceMapper.selectByNamespace(request.getName());
        if (namespace != null) {
            throw new BizException("命名空间已存在");
        }
        Date now = new Date();
        namespace = new MyosotisNamespaceDO();
        namespace.setNamespace(request.getName());
        namespace.setDescription(request.getDescription());
        namespace.setCreateTime(now);
        namespace.setUpdateTime(now);
        namespaceMapper.insert(namespace);

        this.updateNamespaceUsers(namespace.getNamespace(), request.getOwners());
    }

    public List<String> getUserNamespaces() {
        if (SessionContextHolder.getUserRole() == UserRole.SUPERUSER) {
            return null;
        }
        return authorityMapper.listByUsername(SessionContextHolder.getUsername()).stream().map(MyosotisAuthorityDO::getNamespace).collect(Collectors.toList());
    }
}

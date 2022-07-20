package com.yhzdys.myosotis.service;

import com.yhzdys.myosotis.database.mapper.MyosotisAuthorityMapper;
import com.yhzdys.myosotis.database.object.MyosotisAuthorityDO;
import com.yhzdys.myosotis.misc.Const;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthorityService {

    @Resource
    protected MyosotisAuthorityMapper authorityMapper;

    protected void updateNamespaceUsers(String namespace, String usernames) {
        if (StringUtils.isEmpty(usernames)) {
            authorityMapper.deleteByNamespace(namespace);
        }
        Set<String> newUsernames = this.split(usernames);
        newUsernames.remove(Const.default_username);

        Set<String> oldUsernames = authorityMapper.listByNamespace(namespace).stream()
                .map(MyosotisAuthorityDO::getUsername)
                .collect(Collectors.toSet());

        Set<String> added = this.diffAdd(oldUsernames, newUsernames);
        Set<String> deleted = this.diffDel(oldUsernames, newUsernames);
        if (CollectionUtils.isNotEmpty(deleted)) {
            authorityMapper.deleteByUsernames(namespace, deleted);
        }
        if (CollectionUtils.isNotEmpty(added)) {
            List<MyosotisAuthorityDO> authorities = added.stream()
                    .map(u -> new MyosotisAuthorityDO(u, namespace))
                    .collect(Collectors.toList());
            authorityMapper.batchInsert(authorities);
        }
    }

    protected void updateUserNamespaces(String username, String namespaces) {
        if (StringUtils.isEmpty(namespaces)) {
            authorityMapper.deleteByUsername(username);
            return;
        }
        Set<String> newNamespaces = this.split(namespaces);
        List<MyosotisAuthorityDO> list = authorityMapper.listByUsername(username);
        Set<String> oldNamespaces = list.stream()
                .map(MyosotisAuthorityDO::getNamespace)
                .collect(Collectors.toSet());

        Set<String> added = this.diffAdd(oldNamespaces, newNamespaces);
        Set<String> deleted = this.diffDel(oldNamespaces, newNamespaces);
        if (CollectionUtils.isNotEmpty(deleted)) {
            authorityMapper.deleteByNamespaces(username, deleted);
        }
        if (CollectionUtils.isNotEmpty(added)) {
            List<MyosotisAuthorityDO> authorities = added.stream()
                    .map(n -> new MyosotisAuthorityDO(username, n))
                    .collect(Collectors.toList());
            authorityMapper.batchInsert(authorities);
        }
    }

    private Set<String> split(String source) {
        Set<String> set = new HashSet<>();
        for (String string : source.split(",")) {
            if (StringUtils.isEmpty(string)) {
                continue;
            }
            string = string.trim();
            if (StringUtils.isEmpty(string)) {
                continue;
            }
            set.add(string);
        }
        return set;
    }

    private Set<String> diffAdd(Set<String> oldSet, Set<String> newSet) {
        Set<String> set = new HashSet<>(newSet);
        set.removeAll(oldSet);
        return set;
    }

    private Set<String> diffDel(Set<String> oldSet, Set<String> newSet) {
        Set<String> set = new HashSet<>(oldSet);
        set.removeAll(newSet);
        return set;
    }
}

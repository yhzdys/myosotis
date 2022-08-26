package com.yhzdys.myosotis.service;

import com.yhzdys.myosotis.database.mapper.MyosotisConfigMapper;
import com.yhzdys.myosotis.database.mapper.MyosotisNamespaceMapper;
import com.yhzdys.myosotis.database.object.MyosotisConfigDO;
import com.yhzdys.myosotis.database.object.MyosotisNamespaceDO;
import com.yhzdys.myosotis.misc.BizException;
import com.yhzdys.myosotis.misc.Const;
import com.yhzdys.myosotis.misc.PollingTool;
import com.yhzdys.myosotis.web.entity.EditRequest;
import com.yhzdys.myosotis.web.entity.Page;
import com.yhzdys.myosotis.web.entity.vo.ConfigVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ConfigService {

    @Resource
    private MyosotisNamespaceMapper namespaceMapper;
    @Resource
    private MyosotisConfigMapper configMapper;

    public Page page(String namespace, List<String> namespaces, String keyword, Integer page) {
        if (namespaces != null && namespaces.size() < 1) {
            return Page.empty();
        }
        Long count = configMapper.count(namespaces, namespace, keyword);
        if (count < 1) {
            return Page.empty();
        }
        List<MyosotisConfigDO> list = configMapper.list(
                namespaces, namespace, keyword, page * Const.page_size - Const.page_size, Const.page_size
        );
        if (CollectionUtils.isEmpty(list)) {
            return Page.empty();
        }
        List<ConfigVO> voList = list.stream()
                .map(d -> new ConfigVO().convert(d))
                .collect(Collectors.toList());
        return new Page(voList, page, count, (long) page * Const.page_size >= count);
    }

    public ConfigVO get(Long id) {
        MyosotisConfigDO config = configMapper.selectById(id);
        if (config == null) {
            throw new BizException("配置不存在");
        }
        return new ConfigVO().convert(config);
    }

    public void add(EditRequest request) {
        MyosotisConfigDO config = configMapper.selectByKey(request.getNamespace(), request.getName());
        if (config != null) {
            throw new BizException("配置已存在");
        }
        MyosotisNamespaceDO namespace = namespaceMapper.selectByNamespace(request.getNamespace());
        if (namespace == null) {
            throw new BizException("命名空间不存在");
        }
        config = new MyosotisConfigDO();
        config.setNamespace(request.getNamespace());
        config.setConfigKey(request.getName());
        config.setDescription(request.getDescription());
        config.setConfigValue(request.getValue());
        config.setVersion(1);
        Date now = new Date();
        config.setCreateTime(now);
        config.setUpdateTime(now);
        configMapper.insert(config);

        PollingTool.wakeup(config.getNamespace());
    }

    public void update(EditRequest request) {
        MyosotisConfigDO record = configMapper.selectById(request.getId());
        // not real change
        if (Objects.equals(record.getConfigValue(), request.getValue()) &&
                Objects.equals(record.getDescription(), request.getDescription())) {
            return;
        }
        MyosotisConfigDO update = new MyosotisConfigDO();
        update.setId(request.getId());
        update.setDescription(request.getDescription());
        update.setConfigValue(request.getValue());
        update.setUpdateTime(new Date());
        configMapper.update(update);

        PollingTool.wakeup(record.getNamespace());
    }

    public void delete(Long id) {
        MyosotisConfigDO record = configMapper.selectById(id);
        if (record == null) {
            return;
        }
        configMapper.delete(id);

        PollingTool.wakeup(record.getNamespace());
    }
}

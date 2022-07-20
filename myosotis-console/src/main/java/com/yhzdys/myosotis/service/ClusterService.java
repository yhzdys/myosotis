package com.yhzdys.myosotis.service;

import com.yhzdys.myosotis.cluster.ClusterSupport;
import com.yhzdys.myosotis.cluster.Node;
import com.yhzdys.myosotis.misc.Const;
import com.yhzdys.myosotis.web.entity.Page;
import com.yhzdys.myosotis.web.entity.vo.NodeVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClusterService {

    public Page page(Integer page) {
        List<Node> nodes = ClusterSupport.getNodes();
        if (CollectionUtils.isEmpty(nodes)) {
            return Page.empty();
        }
        if (nodes.size() <= Const.page_size) {
            List<NodeVO> list = nodes.stream()
                    .map(n -> new NodeVO().convert(n))
                    .collect(Collectors.toList());
            return new Page(list, page, (long) list.size(), true);
        }
        int start = page * Const.page_size - Const.page_size;
        int end = Math.min(page * Const.page_size, nodes.size());
        List<NodeVO> list = nodes.subList(start, end).stream()
                .map(n -> new NodeVO().convert(n))
                .collect(Collectors.toList());
        return new Page(list, page, (long) list.size(), page * Const.page_size >= nodes.size());
    }

    public void reload() {
        ClusterSupport.reload();
    }
}

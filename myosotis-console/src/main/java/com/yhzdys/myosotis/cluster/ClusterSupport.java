package com.yhzdys.myosotis.cluster;

import com.yhzdys.myosotis.config.cluster.ClusterConfig;
import com.yhzdys.myosotis.config.cluster.ClusterConfigLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClusterSupport {

    private static final Logger logger = LoggerFactory.getLogger(ClusterSupport.class);

    private static List<Node> nodes = null;
    private static ForkJoinPool pool = null;

    public static synchronized void reload() {
        ClusterConfigLoader.reload();
        load();
    }

    public static synchronized void load() {
        List<Node> newNodes = initClusterNodes();
        if (CollectionUtils.isEmpty(newNodes)) {
            return;
        }
        nodes = newNodes;
        if (pool == null) {
            pool = new ForkJoinPool(
                    Math.min(Runtime.getRuntime().availableProcessors(), nodes.size()),
                    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                    (t, e) -> logger.error(e.getMessage(), e),
                    true
            );
        }
        ClusterMonitor.newInstance(pool, nodes).start();
    }

    public static void wakeUp(String namespace) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        pool.execute(
                () -> nodes.parallelStream().forEach(node -> node.wakeUp(namespace))
        );
    }

    private static List<Node> initClusterNodes() {
        ClusterConfig config = ClusterConfigLoader.get();
        if (CollectionUtils.isEmpty(config.getClusterNodes())) {
            return null;
        }
        Pattern pattern = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\:(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[0-5]\\d{4}|[1-9]\\d{0,3})$");
        List<String> nodeAddresses = config.getClusterNodes().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(node -> pattern.matcher(node).matches())
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(nodeAddresses)) {
            return null;
        }
        logger.info("Cluster nodes: {} loaded.", nodeAddresses);
        List<Node> nodeList = nodeAddresses.stream()
                .map(Node::new)
                .collect(Collectors.toList());
        return Collections.unmodifiableList(nodeList);
    }

    public static List<Node> getNodes() {
        return nodes;
    }
}

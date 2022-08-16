package com.yhzdys.myosotis.cluster;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClusterMonitor {

    private final List<Node> nodes;
    private final ScheduledThreadPoolExecutor scheduler;
    private final ForkJoinPool pool;

    private ClusterMonitor(ForkJoinPool pool, List<Node> nodes) {
        this.nodes = nodes;
        this.scheduler = new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("Myosotis-Monitor");
            thread.setDaemon(true);
            return thread;
        });
        this.pool = pool;
    }

    public static ClusterMonitor newInstance(ForkJoinPool pool, List<Node> nodes) {
        return new ClusterMonitor(pool, nodes);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(
                this::checkNodeHealth, 0, 60, TimeUnit.SECONDS
        );
    }

    private void checkNodeHealth() {
        pool.execute(
                () -> nodes.parallelStream().forEach(Node::healthCheck)
        );
    }
}

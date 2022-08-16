package com.yhzdys.myosotis.polling;

import com.yhzdys.myosotis.entity.PollingData;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PollingSupport {

    private static final Logger logger = LoggerFactory.getLogger(PollingSupport.class);

    /**
     * <namespace, <threadId, PollingTask>>
     */
    private static final Map<String, Map<String, PollingTask>> namespaceMap = new ConcurrentHashMap<>(2);

    public static void register(PollingTask pollingTask) {
        List<PollingData> pollingData = pollingTask.getPollingData();
        for (PollingData data : pollingData) {
            try {
                add(data.getNamespace(), pollingTask);
            } catch (Exception e) {
                logger.error("Register [{}] polling task failed, msg: {}", data.getNamespace(), e.getMessage());
            }
        }
    }

    public static void unregister(PollingTask pollingTask) {
        List<PollingData> pollingData = pollingTask.getPollingData();
        for (PollingData data : pollingData) {
            try {
                remove(data.getNamespace(), pollingTask);
            } catch (Exception e) {
                logger.error("Register [{}] polling task failed, msg: {}", data.getNamespace(), e.getMessage());
            }
        }
    }

    public static void wakeUp(String namespace) {
        Map<String, PollingTask> taskMap = namespaceMap.get(namespace);
        if (MapUtils.isEmpty(taskMap)) {
            return;
        }
        for (Map.Entry<String, PollingTask> entry : taskMap.entrySet()) {
            PollingTask pollingTask = entry.getValue();
            if (pollingTask == null) {
                continue;
            }
            pollingTask.wakeUp();
        }
    }

    private static void add(String namespace, PollingTask pollingTask) {
        Map<String, PollingTask> taskMap = namespaceMap.get(namespace);
        if (taskMap != null && taskMap.get(pollingTask.getId()) != null) {
            return;
        }
        taskMap = namespaceMap.computeIfAbsent(
                namespace, cg -> new ConcurrentHashMap<>(2)
        );
        taskMap.putIfAbsent(pollingTask.getId(), pollingTask);
    }

    private static void remove(String namespace, PollingTask pollingTask) {
        Map<String, PollingTask> taskMap = namespaceMap.get(namespace);
        if (MapUtils.isEmpty(taskMap)) {
            return;
        }
        taskMap.remove(pollingTask.getId());
    }
}

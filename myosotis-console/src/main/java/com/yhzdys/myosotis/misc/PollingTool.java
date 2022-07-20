package com.yhzdys.myosotis.misc;

import com.yhzdys.myosotis.cluster.ClusterSupport;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * to wake up local & remote polling thread.
 */
public class PollingTool {

    private static final Logger logger = LoggerFactory.getLogger(PollingTool.class);

    public static void wakeup(String namespace) {
        if (StringUtils.isEmpty(namespace)) {
            logger.error("Namespace is null when wake up polling thread");
            return;
        }
        try {
            ClusterSupport.wakeUp(namespace);
        } catch (Exception e) {
            logger.error("Wake up remote polling task error.", e);
        }
    }
}

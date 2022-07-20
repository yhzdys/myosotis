package com.yhzdys.myosotis;

import com.yhzdys.myosotis.cluster.ClusterSupport;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ClusterInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ClusterSupport.load();
    }
}

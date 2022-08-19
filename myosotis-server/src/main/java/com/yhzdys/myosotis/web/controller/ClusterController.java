package com.yhzdys.myosotis.web.controller;

import com.yhzdys.myosotis.config.server.ServerConfigLoader;
import com.yhzdys.myosotis.polling.PollingSupport;
import com.yhzdys.myosotis.web.WebConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/cluster")
public class ClusterController {

    @Resource
    private WebConfiguration webConfiguration;

    @GetMapping("/health")
    public String health() {
        return webConfiguration.getConnections() + "/" + ServerConfigLoader.get().getKeepAliveRequests();
    }

    @GetMapping("/polling/notify/{namespace}")
    public String pollingNotify(@PathVariable String namespace) {
        PollingSupport.wakeUp(namespace);
        return "success";
    }
}

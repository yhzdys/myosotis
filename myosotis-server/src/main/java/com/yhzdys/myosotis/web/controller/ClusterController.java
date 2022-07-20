package com.yhzdys.myosotis.web.controller;

import com.yhzdys.myosotis.polling.PollingSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cluster")
public class ClusterController {

    @GetMapping("/check")
    public String check() {
        return "success";
    }

    @GetMapping("/polling/notify/{namespace}")
    public String pollingNotify(@PathVariable String namespace) {
        PollingSupport.wakeUp(namespace);
        return "success";
    }

}

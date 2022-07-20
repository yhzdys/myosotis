package com.yhzdys.myosotis.web.controller;

import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.polling.PollingService;
import com.yhzdys.myosotis.polling.PollingTask;
import com.yhzdys.myosotis.web.RequestDeserializer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Callable;

@RestController
public class PollingController {

    @Resource
    private PollingService pollingService;

    @PostMapping("/polling")
    public Callable<byte[]> polling(@RequestBody byte[] data) {
        List<PollingData> pollingData = RequestDeserializer.deserializePollingData(data);
        return new PollingTask(pollingService, pollingData);
    }

}

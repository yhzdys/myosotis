package com.yhzdys.myosotis.web.controller;

import com.yhzdys.myosotis.entity.MyosotisConfig;
import com.yhzdys.myosotis.polling.PollingService;
import com.yhzdys.myosotis.web.ResponseSerializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/query")
public class QueryController {

    @Resource
    private PollingService pollingService;

    @GetMapping("/config/{namespace}/{configKey}")
    public byte[] queryKey(@PathVariable String namespace, @PathVariable String configKey) {
        MyosotisConfig config = pollingService.queryConfig(namespace, configKey);
        return ResponseSerializer.config(config);
    }

    @GetMapping("/namespace/{namespace}")
    public byte[] queryNamespace(@PathVariable String namespace) {
        List<MyosotisConfig> configs = pollingService.queryNamespace(namespace);
        return ResponseSerializer.configs(configs);
    }

    @PostMapping("/configs")
    public byte[] queryConfigs(@RequestBody Map<String, Map<String, Long>> namespaceKeyMap) {
        List<MyosotisConfig> configs = pollingService.queryConfigs(namespaceKeyMap);
        return ResponseSerializer.configs(configs);
    }

}

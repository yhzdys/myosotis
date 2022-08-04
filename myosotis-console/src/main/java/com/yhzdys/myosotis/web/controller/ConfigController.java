package com.yhzdys.myosotis.web.controller;

import com.yhzdys.myosotis.service.ConfigService;
import com.yhzdys.myosotis.service.NamespaceService;
import com.yhzdys.myosotis.service.domain.Menu;
import com.yhzdys.myosotis.service.domain.Permission;
import com.yhzdys.myosotis.web.entity.EditRequest;
import com.yhzdys.myosotis.web.entity.WebResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @Resource
    private ConfigService configService;

    @Resource
    private NamespaceService namespaceService;

    @PostMapping("/add")
    @Permission(Menu.CONFIG)
    public WebResponse add(@RequestBody EditRequest request) {
        configService.add(request);
        return WebResponse.success();
    }

    @GetMapping("/delete/{id}")
    @Permission(Menu.CONFIG)
    public WebResponse delete(@PathVariable Long id) {
        configService.delete(id);
        return WebResponse.success();
    }

    @PostMapping("/update")
    @Permission(Menu.CONFIG)
    public WebResponse update(@RequestBody EditRequest request) {
        configService.update(request);
        return WebResponse.success();
    }

    @GetMapping("/get/{id}")
    @Permission(Menu.CONFIG)
    public WebResponse get(@PathVariable Long id) {
        return WebResponse.success(configService.get(id));
    }

    @GetMapping("/page")
    @Permission(Menu.CONFIG)
    public WebResponse page(@RequestParam(required = false) String namespace,
                            @RequestParam(required = false) Integer page,
                            @RequestParam(required = false) String keyword) {
        namespace = StringUtils.isEmpty(namespace) ? null : namespace;
        page = page == null || page < 1 ? 1 : page;
        keyword = StringUtils.isEmpty(keyword) ? null : keyword;
        return WebResponse.success(
                configService.page(namespace, namespaceService.getUserNamespaces(), keyword, page)
        );
    }
}

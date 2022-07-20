package com.yhzdys.myosotis.web.controller;

import com.yhzdys.myosotis.service.ClusterService;
import com.yhzdys.myosotis.service.domain.Menu;
import com.yhzdys.myosotis.service.domain.Permission;
import com.yhzdys.myosotis.web.entity.WebResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/server")
public class ServerController {

    @Resource
    private ClusterService clusterService;

    @GetMapping("/page")
    @Permission(Menu.SERVER)
    public WebResponse page(@RequestParam(required = false) Integer page) {
        page = page == null || page < 1 ? 1 : page;
        return WebResponse.success(clusterService.page(page));
    }

    @GetMapping("/reload")
    @Permission(Menu.SERVER)
    public WebResponse reload() {
        clusterService.reload();
        return WebResponse.success();
    }
}

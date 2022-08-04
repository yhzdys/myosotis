package com.yhzdys.myosotis.web.controller;

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
@RequestMapping("/namespace")
public class NamespaceController {

    @Resource
    private NamespaceService namespaceService;

    @PostMapping("/add")
    @Permission(Menu.NAMESPACE)
    public WebResponse add(@RequestBody EditRequest request) {
        namespaceService.add(request);
        return WebResponse.success();
    }

    @GetMapping("/delete/{id}")
    @Permission(Menu.NAMESPACE)
    public WebResponse delete(@PathVariable Long id) {
        namespaceService.delete(id);
        return WebResponse.success();
    }

    @PostMapping("/update")
    @Permission(Menu.NAMESPACE)
    public WebResponse update(@RequestBody EditRequest request) {
        namespaceService.update(request);
        return WebResponse.success();
    }

    @GetMapping("/get/{id}")
    @Permission(Menu.NAMESPACE)
    public WebResponse get(@PathVariable Long id) {
        return WebResponse.success(namespaceService.get(id));
    }

    @GetMapping("/page")
    @Permission(Menu.NAMESPACE)
    public WebResponse page(@RequestParam(required = false) Integer page,
                            @RequestParam(required = false) String keyword) {
        page = page == null || page < 1 ? 1 : page;
        keyword = StringUtils.isEmpty(keyword) ? null : keyword;
        return WebResponse.success(namespaceService.page(keyword, page));
    }
}
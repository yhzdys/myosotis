package com.yhzdys.myosotis.web.controller;

import com.yhzdys.myosotis.misc.SessionUtil;
import com.yhzdys.myosotis.service.UserService;
import com.yhzdys.myosotis.service.domain.Menu;
import com.yhzdys.myosotis.service.domain.PairEnum;
import com.yhzdys.myosotis.service.domain.Permission;
import com.yhzdys.myosotis.service.domain.SessionContext;
import com.yhzdys.myosotis.service.domain.UserRole;
import com.yhzdys.myosotis.web.entity.SessionContextHolder;
import com.yhzdys.myosotis.web.entity.UserRequest;
import com.yhzdys.myosotis.web.entity.WebResponse;
import com.yhzdys.myosotis.web.entity.vo.PairVO;
import com.yhzdys.myosotis.web.entity.vo.UserIndexVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/index")
    public WebResponse index() {
        SessionContext session = SessionContextHolder.get();
        UserIndexVO userIndex = new UserIndexVO();
        userIndex.setUserId(session.getUserId());
        userIndex.setUsername(session.getUsername());
        userIndex.setUserRole(session.getUserRole().toPair());
        userIndex.setMenus(session.getMenus().stream().map(PairEnum::toPair).collect(Collectors.toList()));
        return WebResponse.success(userIndex);
    }

    @PostMapping("/add")
    @Permission(Menu.USER)
    public WebResponse add(@RequestBody UserRequest request) {
        userService.add(request);
        return WebResponse.success();
    }

    @GetMapping("/delete/{id}")
    @Permission(Menu.USER)
    public WebResponse delete(@PathVariable Long id) {
        userService.delete(id);
        return WebResponse.success();
    }

    @PostMapping("/update")
    @Permission(Menu.USER)
    public WebResponse update(@RequestBody UserRequest request) {
        userService.update(request);
        return WebResponse.success();
    }

    @GetMapping("/get/{id}")
    @Permission(Menu.USER)
    public WebResponse get(@PathVariable Long id) {
        return WebResponse.success(userService.getById(id));
    }

    @GetMapping("/role")
    @Permission(Menu.USER)
    public WebResponse role() {
        List<PairVO> voList = Arrays.stream(UserRole.values())
                .filter(role -> role != UserRole.SUPERUSER)
                .map(PairEnum::toPair)
                .collect(Collectors.toList());
        return WebResponse.success(voList);
    }

    @GetMapping("/page")
    @Permission(Menu.USER)
    public WebResponse page(@RequestParam(required = false) Integer page,
                            @RequestParam(required = false) String keyword) {
        page = page == null || page < 1 ? 1 : page;
        keyword = StringUtils.isEmpty(keyword) ? null : keyword;
        return WebResponse.success(userService.page(keyword, page));
    }

    @GetMapping("/reset/{id}")
    @Permission(Menu.USER)
    public WebResponse reset(@PathVariable Long id) {
        String username = userService.resetPassword(id);
        if (Objects.equals(SessionContextHolder.getUsername(), username)) {
            SessionUtil.clearSession();
        }
        return WebResponse.success();
    }
}

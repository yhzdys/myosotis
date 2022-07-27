package com.yhzdys.myosotis.web.controller;

import com.yhzdys.myosotis.misc.SessionUtil;
import com.yhzdys.myosotis.service.SessionService;
import com.yhzdys.myosotis.service.domain.UserSession;
import com.yhzdys.myosotis.web.entity.WebResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/session")
public class SessionController {

    @Resource
    private SessionService sessionService;

    @GetMapping("/key")
    public WebResponse key(@RequestParam String username) {
        String key = sessionService.getLoginKey(username);
        return WebResponse.success(key);
    }

    @GetMapping("/logout")
    public WebResponse logout() {
        sessionService.logout();
        SessionUtil.clearSession();
        return WebResponse.success();
    }

    @PostMapping("/password")
    public WebResponse password(@RequestBody Map<String, String> param) {
        String username = param.get("username");
        String oldPassword = param.get("oldPassword");
        String password = param.get("password");
        sessionService.password(username, oldPassword, password);
        SessionUtil.clearSession();
        return WebResponse.success();
    }

    @PostMapping("/login")
    public WebResponse login(@RequestBody Map<String, String> param) {
        String username = param.get("username");
        String password = param.get("password");
        UserSession session = sessionService.login(username, password);
        SessionUtil.setSession(session);
        return WebResponse.success(session.getUser());
    }
}

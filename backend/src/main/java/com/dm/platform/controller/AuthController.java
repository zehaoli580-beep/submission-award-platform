package com.dm.platform.controller;

import com.dm.platform.common.Result;
import com.dm.platform.dto.LoginRequest;
import com.dm.platform.dto.RegisterRequest;
import com.dm.platform.dto.UserView;
import com.dm.platform.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口: 注册 / 登录 / 登出 / 当前用户
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<UserView> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(authService.register(req));
    }

    @PostMapping("/login")
    public Result<UserView> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        return Result.ok(authService.login(req, session));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        authService.logout(session);
        return Result.ok();
    }

    /** 前端刷新页面时用来恢复登录态 */
    @GetMapping("/me")
    public Result<UserView> me(HttpSession session) {
        UserView user = (UserView) session.getAttribute(AuthService.SESSION_KEY);
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.ok(user);
    }
}

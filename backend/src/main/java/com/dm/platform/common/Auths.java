package com.dm.platform.common;

import com.dm.platform.dto.UserView;
import com.dm.platform.service.AuthService;
import jakarta.servlet.http.HttpSession;

/**
 * 会话取用户 / 角色校验的小工具, 各 Controller 复用
 */
public final class Auths {

    public static UserView user(HttpSession session) {
        UserView user = (UserView) session.getAttribute(AuthService.SESSION_KEY);
        if (user == null) {
            throw new BusinessException("请先登录");
        }
        return user;
    }

    public static UserView requireRole(HttpSession session, String... roles) {
        UserView user = user(session);
        for (String role : roles) {
            if (user.getRole().equals(role)) {
                return user;
            }
        }
        throw new BusinessException("无权限: 该操作仅限 " + String.join("/", roles));
    }

    private Auths() {
    }
}

package com.dm.platform.interceptor;

import com.dm.platform.common.Result;
import com.dm.platform.dto.UserView;
import com.dm.platform.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器: /api/** 下除白名单外, 未登录一律 401
 * (Session 认证的"守门员", 报告联调章节会讲它的作用)
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        UserView user = session == null ? null
                : (UserView) session.getAttribute(AuthService.SESSION_KEY);
        if (user != null) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error("请先登录")));
        return false;
    }
}

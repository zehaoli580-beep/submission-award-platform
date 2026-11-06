package com.dm.platform.service;

import com.dm.platform.common.BusinessException;
import com.dm.platform.dto.LoginRequest;
import com.dm.platform.dto.RegisterRequest;
import com.dm.platform.dto.UserView;
import com.dm.platform.entity.User;
import com.dm.platform.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务: 注册 / 登录 / 登出
 * 认证方式为 Session(课程作业够用, 生产可演进为 JWT, 报告里会讨论两者取舍)
 */
@Service
public class AuthService {

    public static final String SESSION_KEY = "loginUser";

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /** 注册: 用户名查重 + BCrypt 加密落库 */
    public UserView register(RegisterRequest req) {
        if (userMapper.findByUsername(req.getUsername()) != null) {
            throw new BusinessException("用户名已被注册");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setNickname(req.getNickname());
        userMapper.insert(user);
        return toView(user);
    }

    /** 登录: 校验密码, 把脱敏后的用户信息放进 Session */
    public UserView login(LoginRequest req, jakarta.servlet.http.HttpSession session) {
        User user = userMapper.findByUsername(req.getUsername());
        if (user == null || !encoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        UserView view = toView(user);
        session.setAttribute(SESSION_KEY, view);
        return view;
    }

    public void logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();
    }

    public static UserView toView(User user) {
        return new UserView(user.getId(), user.getUsername(), user.getRole(), user.getNickname());
    }
}

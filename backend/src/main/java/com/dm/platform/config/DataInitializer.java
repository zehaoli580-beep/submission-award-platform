package com.dm.platform.config;

import com.dm.platform.entity.User;
import com.dm.platform.mapper.UserMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 演示数据初始化: 首次启动(user 表为空)时自动创建三个内置账号,
 * 方便一装好就跑通"作者投稿 -> 管理员初审 -> 审稿人评审"的演示流程
 *
 * 内置账号(密码见下, 仅供课程演示):
 *   admin    / admin123   管理员
 *   reviewer / review123  审稿人
 *   author   / author123  作者
 */
@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner initDemoUsers(UserMapper userMapper) {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            // 按用户名逐个补齐演示账号(已存在则跳过), 便于后续版本扩展新账号
            ensureUser(userMapper, encoder, "admin", "admin123", "ADMIN", "平台管理员");
            ensureUser(userMapper, encoder, "reviewer", "review123", "REVIEWER", "评审老师");
            ensureUser(userMapper, encoder, "reviewer2", "review123", "REVIEWER", "评审老师二");
            ensureUser(userMapper, encoder, "author", "author123", "AUTHOR", "示例作者");
        };
    }

    private void ensureUser(UserMapper userMapper, BCryptPasswordEncoder encoder,
                            String username, String rawPassword, String role, String nickname) {
        if (userMapper.findByUsername(username) != null) {
            return;
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(rawPassword));
        u.setRole(role);
        u.setNickname(nickname);
        userMapper.insert(u);
    }
}

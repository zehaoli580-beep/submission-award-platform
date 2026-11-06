package com.dm.platform.entity;

import java.time.LocalDateTime;

/**
 * 用户实体, 对应表 user
 */
public class User {

    private Long id;
    private String username;
    private String password;   // BCrypt 密文, 任何接口都不会把它返回给前端
    private String role;       // AUTHOR / REVIEWER / ADMIN
    private String nickname;
    private LocalDateTime createTime;

    // getter / setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

package com.dm.platform.dto;

/**
 * 返回给前端的用户信息(脱敏: 不含密码)
 */
public class UserView {

    private Long id;
    private String username;
    private String role;
    private String nickname;

    public UserView(Long id, String username, String role, String nickname) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.nickname = nickname;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getNickname() {
        return nickname;
    }
}

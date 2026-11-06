package com.dm.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册请求体
 */
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度 3-50")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度 6-64")
    private String password;

    /** 只能注册作者或审稿人, 管理员不开放注册 */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "AUTHOR|REVIEWER", message = "角色只能是 AUTHOR 或 REVIEWER")
    private String role;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最长 50 字")
    private String nickname;

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
}

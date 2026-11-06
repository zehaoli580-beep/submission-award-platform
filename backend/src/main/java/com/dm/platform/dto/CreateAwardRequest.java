package com.dm.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建奖项批次请求体
 */
public class CreateAwardRequest {

    @NotBlank(message = "奖项名称不能为空")
    @Size(max = 100, message = "奖项名称最长 100 字")
    private String name;

    @Size(max = 500, message = "奖项说明最长 500 字")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

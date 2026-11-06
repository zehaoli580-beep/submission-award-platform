package com.dm.platform.entity;

import java.time.LocalDateTime;

/**
 * 奖项批次实体, 对应表 award
 */
public class Award {

    private Long id;
    private String name;
    private String description;
    private String status; // PREPARING-筹备 / REVIEWING-评审中 / PUBLISHED-已公示 / ARCHIVED-已归档
    private LocalDateTime createTime;
    private LocalDateTime publishTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }
}

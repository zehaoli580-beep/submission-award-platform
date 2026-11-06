package com.dm.platform.entity;

import java.time.LocalDateTime;

/**
 * 稿件实体, 对应表 manuscript
 */
public class Manuscript {

    private Long id;
    private Long authorId;
    private String title;
    private String category;    // 作品分类: 短视频/平面设计/动画/摄影...
    private String description; // 作品简介
    private String fileName;    // 作者上传的原始文件名
    private String fileUrl;     // 访问路径 /files/xxx (文件本体在磁盘/对象存储, 不进数据库)
    private Long fileSize;      // 字节数
    private String status;      // 状态机, 见 ManuscriptStatus
    private LocalDateTime submitTime;
    private LocalDateTime updateTime;

    // getter / setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

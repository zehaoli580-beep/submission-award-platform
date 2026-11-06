package com.dm.platform.dto;

import java.time.LocalDateTime;

/**
 * 审稿人的评审任务视图: 分配记录 + 稿件信息合并展示
 */
public class ReviewTaskView {

    private Long assignmentId;
    private Long manuscriptId;
    private String title;
    private String category;
    private String description;
    private String fileName;
    private String fileUrl;
    private String manuscriptStatus;
    private String assignStatus;   // ASSIGNED / DONE
    private LocalDateTime submitTime;

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getManuscriptId() {
        return manuscriptId;
    }

    public void setManuscriptId(Long manuscriptId) {
        this.manuscriptId = manuscriptId;
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

    public String getManuscriptStatus() {
        return manuscriptStatus;
    }

    public void setManuscriptStatus(String manuscriptStatus) {
        this.manuscriptStatus = manuscriptStatus;
    }

    public String getAssignStatus() {
        return assignStatus;
    }

    public void setAssignStatus(String assignStatus) {
        this.assignStatus = assignStatus;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }
}

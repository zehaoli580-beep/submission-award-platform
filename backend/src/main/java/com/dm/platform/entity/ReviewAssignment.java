package com.dm.platform.entity;

import java.time.LocalDateTime;

/**
 * 评审分配实体, 对应表 review_assignment
 */
public class ReviewAssignment {

    private Long id;
    private Long manuscriptId;
    private Long reviewerId;
    private String status; // ASSIGNED-待审 / DONE-已提交意见
    private LocalDateTime assignTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getManuscriptId() {
        return manuscriptId;
    }

    public void setManuscriptId(Long manuscriptId) {
        this.manuscriptId = manuscriptId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAssignTime() {
        return assignTime;
    }

    public void setAssignTime(LocalDateTime assignTime) {
        this.assignTime = assignTime;
    }
}

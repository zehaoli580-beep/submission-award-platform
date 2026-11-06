package com.dm.platform.entity;

import java.time.LocalDateTime;

/**
 * 审稿意见实体, 对应表 review_opinion
 * 一条分配记录对应一份意见(数据库唯一键保证"一人一票")
 */
public class ReviewOpinion {

    private Long id;
    private Long assignmentId;
    private Integer score;          // 0-100
    private String comment;         // 评审意见
    private String recommendation;  // ACCEPT-录用 / REVISE-退修改 / REJECT-退稿
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

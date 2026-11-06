package com.dm.platform.entity;

import java.time.LocalDateTime;

/**
 * 提名打分记录实体, 对应表 nomination_score
 * 唯一键(nomination_id, judge_id)保证每个评委对每个作品只打一次分
 */
public class NominationScore {

    private Long id;
    private Long nominationId;
    private Long judgeId;
    private Integer score;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNominationId() {
        return nominationId;
    }

    public void setNominationId(Long nominationId) {
        this.nominationId = nominationId;
    }

    public Long getJudgeId() {
        return judgeId;
    }

    public void setJudgeId(Long judgeId) {
        this.judgeId = judgeId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

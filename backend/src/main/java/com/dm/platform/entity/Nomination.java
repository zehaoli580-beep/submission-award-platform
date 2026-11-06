package com.dm.platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提名记录实体, 对应表 nomination
 */
public class Nomination {

    private Long id;
    private Long awardId;
    private Long manuscriptId;
    private BigDecimal finalScore; // 终评得分(多评委去最高最低后均值), 公示时计算
    private String result;         // NOMINATED-提名 / WON-获奖 / LOST-落选
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAwardId() {
        return awardId;
    }

    public void setAwardId(Long awardId) {
        this.awardId = awardId;
    }

    public Long getManuscriptId() {
        return manuscriptId;
    }

    public void setManuscriptId(Long manuscriptId) {
        this.manuscriptId = manuscriptId;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

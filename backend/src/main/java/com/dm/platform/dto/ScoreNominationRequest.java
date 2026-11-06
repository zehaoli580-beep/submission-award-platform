package com.dm.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 评委给提名作品打分请求体
 */
public class ScoreNominationRequest {

    @NotNull(message = "评分不能为空")
    @Min(value = 0, message = "评分最低 0 分")
    @Max(value = 100, message = "评分最高 100 分")
    private Integer score;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}

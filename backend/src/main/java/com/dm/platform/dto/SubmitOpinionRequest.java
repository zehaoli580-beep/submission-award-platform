package com.dm.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 审稿人提交意见请求体
 */
public class SubmitOpinionRequest {

    @NotNull(message = "评分不能为空")
    @Min(value = 0, message = "评分最低 0 分")
    @Max(value = 100, message = "评分最高 100 分")
    private Integer score;

    @Size(max = 1000, message = "评审意见最长 1000 字")
    private String comment;

    /** 推荐结论: 录用 / 退修改 / 退稿 */
    @NotNull(message = "推荐结论不能为空")
    @Pattern(regexp = "ACCEPT|REVISE|REJECT", message = "结论只能是 ACCEPT / REVISE / REJECT")
    private String recommendation;

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
}

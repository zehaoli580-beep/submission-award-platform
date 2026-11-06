package com.dm.platform.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * 管理员操作请求体集合: 初审通过+分配 / 追加分配 / 终审决议
 */
public class AdminRequests {

    /** 初审通过并分配审稿人(或对 UNDER_REVIEW 稿件追加审稿人) */
    public static class AssignRequest {
        @NotEmpty(message = "至少选择一名审稿人")
        private List<Long> reviewerIds;

        public List<Long> getReviewerIds() {
            return reviewerIds;
        }

        public void setReviewerIds(List<Long> reviewerIds) {
            this.reviewerIds = reviewerIds;
        }
    }

    /** 终审决议 */
    public static class FinalRequest {
        @NotNull(message = "决议不能为空")
        @Pattern(regexp = "ACCEPTED|REVISE|REJECTED", message = "决议只能是 ACCEPTED / REVISE / REJECTED")
        private String decision;

        public String getDecision() {
            return decision;
        }

        public void setDecision(String decision) {
            this.decision = decision;
        }
    }
}

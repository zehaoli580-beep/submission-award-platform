package com.dm.platform.dto;

/**
 * 管理端首页仪表盘统计数据(各状态稿件数/用户数/奖项数)
 */
public class AdminStats {

    private long pendingCount;      // 待初审
    private long reviewingCount;    // 外审中
    private long reviseCount;       // 退修改
    private long acceptedCount;     // 已录用
    private long rejectedCount;     // 已退稿
    private long authorCount;       // 作者数
    private long reviewerCount;     // 审稿人数
    private long awardCount;        // 奖项总数
    private long publishedAwardCount; // 已公示奖项

    public long getPendingCount() { return pendingCount; }
    public void setPendingCount(long v) { this.pendingCount = v; }

    public long getReviewingCount() { return reviewingCount; }
    public void setReviewingCount(long v) { this.reviewingCount = v; }

    public long getReviseCount() { return reviseCount; }
    public void setReviseCount(long v) { this.reviseCount = v; }

    public long getAcceptedCount() { return acceptedCount; }
    public void setAcceptedCount(long v) { this.acceptedCount = v; }

    public long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(long v) { this.rejectedCount = v; }

    public long getAuthorCount() { return authorCount; }
    public void setAuthorCount(long v) { this.authorCount = v; }

    public long getReviewerCount() { return reviewerCount; }
    public void setReviewerCount(long v) { this.reviewerCount = v; }

    public long getAwardCount() { return awardCount; }
    public void setAwardCount(long v) { this.awardCount = v; }

    public long getPublishedAwardCount() { return publishedAwardCount; }
    public void setPublishedAwardCount(long v) { this.publishedAwardCount = v; }
}

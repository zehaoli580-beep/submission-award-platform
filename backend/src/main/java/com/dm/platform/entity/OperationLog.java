package com.dm.platform.entity;

import java.time.LocalDateTime;

/**
 * 操作日志实体, 对应表 operation_log
 * 状态机每流转一次就插一条: 谁(operator)在什么时候把稿件从 from_status 改成了 to_status
 */
public class OperationLog {

    private Long id;
    private Long manuscriptId;
    private Long operatorId;
    private String action;
    private String fromStatus;
    private String toStatus;
    private String detail;
    private LocalDateTime createTime;

    // getter / setter
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

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

package com.gov.workflow.enums;

public enum CaseStatus {
    DRAFT("草稿"),
    SUBMITTED("已提交"),
    PENDING_ACCEPT("待受理"),
    ACCEPTED("已受理"),
    PENDING_SUPPLEMENT("待补正"),
    IN_APPROVAL("审批中"),
    APPROVED("审批通过"),
    REJECTED("审批驳回"),
    COMPLETED("已办结"),
    WITHDRAWN("已撤回"),
    OVERDUE("已超期");

    private final String description;

    CaseStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package com.gov.workflow.enums;

public enum ApprovalResult {
    PASS("通过"),
    REJECT("驳回"),
    RETURN_TO_SUPPLEMENT("退回补正"),
    TRANSFER("转交");

    private final String description;

    ApprovalResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package com.gov.workflow.entity;

import com.gov.workflow.enums.ApprovalResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRecord {
    private String id;
    private String caseId;
    private Integer nodeOrder;
    private String nodeName;
    private String approver;
    private ApprovalResult result;
    private String comment;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}

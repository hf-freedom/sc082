package com.gov.workflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupervisionRecord {
    private String id;
    private String caseId;
    private String caseNumber;
    private String itemName;
    private String applicantName;
    private String handler;
    private LocalDateTime dueDate;
    private LocalDateTime supervisionTime;
    private Long overdueHours;
    private String remark;
    private String status;
    private LocalDateTime createdAt;
}

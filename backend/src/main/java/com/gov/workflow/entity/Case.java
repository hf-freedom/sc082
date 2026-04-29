package com.gov.workflow.entity;

import com.gov.workflow.enums.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Case {
    private String id;
    private String caseNumber;
    private String itemId;
    private String itemName;
    private String applicantName;
    private String applicantIdCard;
    private String applicantPhone;
    private String applicantAddress;
    private CaseStatus status;
    private List<SubmittedMaterial> submittedMaterials;
    private List<String> missingMaterialIds;
    private Integer currentNodeOrder;
    private String currentNodeName;
    private String handler;
    private LocalDateTime submittedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private LocalDateTime withdrawnAt;
    private String resultDocumentId;
    private String eLicenseId;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean overdue;
    private boolean warningSent;
}

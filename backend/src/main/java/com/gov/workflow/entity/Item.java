package com.gov.workflow.entity;

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
public class Item {
    private String id;
    private String name;
    private String code;
    private String description;
    private String category;
    private Integer processingDays;
    private Integer warningDays;
    private List<String> requiredMaterialIds;
    private List<String> optionalMaterialIds;
    private List<ApprovalNode> approvalNodes;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

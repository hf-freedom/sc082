package com.gov.workflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalNode {
    private Integer order;
    private String name;
    private String role;
    private List<String> assignees;
    private boolean autoApprove;
}

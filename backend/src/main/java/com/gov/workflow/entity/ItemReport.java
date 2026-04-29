package com.gov.workflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemReport {
    private String itemId;
    private String itemName;
    private Integer totalCount;
    private Integer completedCount;
    private Integer overdueCount;
    private Integer returnedCount;
    private Double overdueRate;
    private Double returnRate;
}

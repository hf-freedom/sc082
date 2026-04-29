package com.gov.workflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReport {
    private String id;
    private LocalDate reportDate;
    private Integer totalCases;
    private Integer acceptedCases;
    private Integer completedCases;
    private Integer overdueCases;
    private Integer returnedCases;
    private Integer withdrawnCases;
    private Double overdueRate;
    private Double returnRate;
    private List<ItemReport> itemReports;
    private LocalDateTime createdAt;
}

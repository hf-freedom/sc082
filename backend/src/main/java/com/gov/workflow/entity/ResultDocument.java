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
public class ResultDocument {
    private String id;
    private String caseId;
    private String documentNumber;
    private String documentType;
    private String documentName;
    private String content;
    private LocalDateTime issuedAt;
    private LocalDateTime createdAt;
}

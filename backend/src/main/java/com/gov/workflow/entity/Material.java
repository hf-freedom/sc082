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
public class Material {
    private String id;
    private String name;
    private String code;
    private String description;
    private boolean required;
    private String fileType;
    private Long maxSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

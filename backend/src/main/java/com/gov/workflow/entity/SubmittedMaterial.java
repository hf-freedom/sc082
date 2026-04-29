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
public class SubmittedMaterial {
    private String materialId;
    private String materialName;
    private String fileId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime submittedAt;
    private boolean valid;
    private String comment;
}

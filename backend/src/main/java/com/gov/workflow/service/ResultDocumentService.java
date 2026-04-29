package com.gov.workflow.service;

import com.gov.workflow.entity.Case;
import com.gov.workflow.entity.ResultDocument;
import com.gov.workflow.repository.CaseRepository;
import com.gov.workflow.repository.ResultDocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class ResultDocumentService {

    @Autowired
    private ResultDocumentRepository resultDocumentRepository;

    @Autowired
    private CaseRepository caseRepository;

    public ResultDocument generateResultDocument(String caseId, String documentType, String content) {
        Optional<Case> caseOpt = caseRepository.findById(caseId);
        if (!caseOpt.isPresent()) {
            throw new RuntimeException("办件不存在");
        }
        Case caseEntity = caseOpt.get();

        Optional<ResultDocument> existingDoc = resultDocumentRepository.findByCaseId(caseId);
        if (existingDoc.isPresent()) {
            return existingDoc.get();
        }

        String documentName = buildDocumentName(documentType, caseEntity);

        ResultDocument document = ResultDocument.builder()
                .caseId(caseId)
                .documentType(documentType)
                .documentName(documentName)
                .content(content != null ? content : generateDefaultContent(documentType, caseEntity))
                .issuedAt(LocalDateTime.now())
                .build();

        document = resultDocumentRepository.save(document);

        if ("ELICENSE".equals(documentType)) {
            caseEntity.setELicenseId(document.getId());
        } else {
            caseEntity.setResultDocumentId(document.getId());
        }
        caseRepository.save(caseEntity);

        log.info("为办件 {} 生成结果文档: {} (编号: {})",
                caseEntity.getCaseNumber(), documentName, document.getDocumentNumber());

        return document;
    }

    private String buildDocumentName(String documentType, Case caseEntity) {
        if ("ELICENSE".equals(documentType)) {
            return caseEntity.getItemName() + "-电子证照";
        }
        return caseEntity.getItemName() + "-办理结果";
    }

    private String generateDefaultContent(String documentType, Case caseEntity) {
        StringBuilder sb = new StringBuilder();

        if ("ELICENSE".equals(documentType)) {
            sb.append("【电子证照】\n\n");
            sb.append("证照编号: ").append(caseEntity.getCaseNumber()).append("\n");
            sb.append("事项名称: ").append(caseEntity.getItemName()).append("\n");
            sb.append("持证人: ").append(caseEntity.getApplicantName()).append("\n");
            sb.append("身份证号: ").append(caseEntity.getApplicantIdCard()).append("\n");
            sb.append("有效期: 长期有效\n");
            sb.append("发证机关: 政务服务中心\n");
            sb.append("发证日期: ").append(java.time.LocalDate.now().toString()).append("\n");
        } else {
            sb.append("【办理结果通知书】\n\n");
            sb.append("案件编号: ").append(caseEntity.getCaseNumber()).append("\n");
            sb.append("事项名称: ").append(caseEntity.getItemName()).append("\n");
            sb.append("申请人: ").append(caseEntity.getApplicantName()).append("\n");
            sb.append("受理日期: ").append(caseEntity.getAcceptedAt() != null ?
                    caseEntity.getAcceptedAt().toLocalDate().toString() : "N/A").append("\n");
            sb.append("办结日期: ").append(caseEntity.getCompletedAt() != null ?
                    caseEntity.getCompletedAt().toLocalDate().toString() : "N/A").append("\n");
            sb.append("\n");
            sb.append("办理结果: 同意\n");
            sb.append("备注: 材料齐全，符合办理条件。\n");
        }

        return sb.toString();
    }

    public Optional<ResultDocument> findById(String id) {
        return resultDocumentRepository.findById(id);
    }

    public Optional<ResultDocument> findByCaseId(String caseId) {
        return resultDocumentRepository.findByCaseId(caseId);
    }

    public Optional<ResultDocument> findByDocumentNumber(String documentNumber) {
        return resultDocumentRepository.findByDocumentNumber(documentNumber);
    }
}

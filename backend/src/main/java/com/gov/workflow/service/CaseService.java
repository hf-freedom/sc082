package com.gov.workflow.service;

import com.gov.workflow.entity.ApprovalNode;
import com.gov.workflow.entity.ApprovalRecord;
import com.gov.workflow.entity.Case;
import com.gov.workflow.entity.Item;
import com.gov.workflow.entity.Material;
import com.gov.workflow.entity.SubmittedMaterial;
import com.gov.workflow.enums.ApprovalResult;
import com.gov.workflow.enums.CaseStatus;
import com.gov.workflow.repository.ApprovalRecordRepository;
import com.gov.workflow.repository.CaseRepository;
import com.gov.workflow.repository.ItemRepository;
import com.gov.workflow.repository.MaterialRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CaseService {

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ApprovalRecordRepository approvalRecordRepository;

    public List<Case> findAll() {
        return caseRepository.findAll();
    }

    public Optional<Case> findById(String id) {
        return caseRepository.findById(id);
    }

    public Optional<Case> findByCaseNumber(String caseNumber) {
        return caseRepository.findByCaseNumber(caseNumber);
    }

    public List<Case> findByStatus(CaseStatus status) {
        return caseRepository.findByStatus(status);
    }

    public Case submitCase(Case caseEntity) {
        List<Case> uncompletedCases = caseRepository.findUncompletedByItemIdAndApplicant(
                caseEntity.getItemId(),
                caseEntity.getApplicantIdCard()
        );
        if (!uncompletedCases.isEmpty()) {
            throw new RuntimeException("该事项存在未办结记录，不可重复申请。未办结案件号：" +
                    uncompletedCases.stream().map(Case::getCaseNumber).collect(Collectors.joining(", ")));
        }

        Optional<Item> itemOpt = itemRepository.findById(caseEntity.getItemId());
        if (!itemOpt.isPresent()) {
            throw new RuntimeException("事项不存在");
        }
        Item item = itemOpt.get();
        caseEntity.setItemName(item.getName());

        caseEntity.setStatus(CaseStatus.SUBMITTED);
        caseEntity.setSubmittedAt(LocalDateTime.now());
        caseEntity.setOverdue(false);
        caseEntity.setWarningSent(false);

        List<String> missingMaterials = checkRequiredMaterials(
                item.getRequiredMaterialIds(),
                caseEntity.getSubmittedMaterials()
        );
        caseEntity.setMissingMaterialIds(missingMaterials);

        if (!missingMaterials.isEmpty()) {
            caseEntity.setStatus(CaseStatus.PENDING_SUPPLEMENT);
            log.info("办件 {} 材料缺失，进入待补正状态", caseEntity.getCaseNumber());
        } else {
            caseEntity.setStatus(CaseStatus.PENDING_ACCEPT);
            log.info("办件 {} 材料完整，进入待受理状态", caseEntity.getCaseNumber());
        }

        return caseRepository.save(caseEntity);
    }

    private List<String> checkRequiredMaterials(List<String> requiredMaterialIds,
                                                 List<SubmittedMaterial> submittedMaterials) {
        if (submittedMaterials == null) {
            submittedMaterials = new ArrayList<>();
        }
        List<String> submittedIds = submittedMaterials.stream()
                .filter(SubmittedMaterial::isValid)
                .map(SubmittedMaterial::getMaterialId)
                .collect(Collectors.toList());

        return requiredMaterialIds.stream()
                .filter(id -> !submittedIds.contains(id))
                .collect(Collectors.toList());
    }

    public Case acceptCase(String caseId, String handler) {
        Optional<Case> caseOpt = caseRepository.findById(caseId);
        if (!caseOpt.isPresent()) {
            throw new RuntimeException("办件不存在");
        }
        Case caseEntity = caseOpt.get();

        if (!CaseStatus.PENDING_ACCEPT.equals(caseEntity.getStatus())) {
            throw new RuntimeException("当前状态不允许受理");
        }

        Optional<Item> itemOpt = itemRepository.findById(caseEntity.getItemId());
        if (!itemOpt.isPresent()) {
            throw new RuntimeException("事项不存在");
        }
        Item item = itemOpt.get();

        caseEntity.setStatus(CaseStatus.ACCEPTED);
        caseEntity.setAcceptedAt(LocalDateTime.now());
        caseEntity.setHandler(handler);
        caseEntity.setDueDate(LocalDateTime.now().plusDays(item.getProcessingDays()));

        if (item.getApprovalNodes() != null && !item.getApprovalNodes().isEmpty()) {
            ApprovalNode firstNode = item.getApprovalNodes().get(0);
            caseEntity.setCurrentNodeOrder(firstNode.getOrder());
            caseEntity.setCurrentNodeName(firstNode.getName());
        }

        caseEntity.setStatus(CaseStatus.IN_APPROVAL);

        log.info("办件 {} 已受理，进入审批流程，办理时限至 {}",
                caseEntity.getCaseNumber(), caseEntity.getDueDate());

        return caseRepository.save(caseEntity);
    }

    public Case supplementMaterials(String caseId, List<SubmittedMaterial> supplementaryMaterials) {
        Optional<Case> caseOpt = caseRepository.findById(caseId);
        if (!caseOpt.isPresent()) {
            throw new RuntimeException("办件不存在");
        }
        Case caseEntity = caseOpt.get();

        if (!CaseStatus.PENDING_SUPPLEMENT.equals(caseEntity.getStatus())) {
            throw new RuntimeException("当前状态不允许补正");
        }

        if (caseEntity.getSubmittedMaterials() == null) {
            caseEntity.setSubmittedMaterials(new ArrayList<>());
        }

        for (SubmittedMaterial material : supplementaryMaterials) {
            material.setSubmittedAt(LocalDateTime.now());
            material.setValid(true);
            caseEntity.getSubmittedMaterials().add(material);
        }

        Optional<Item> itemOpt = itemRepository.findById(caseEntity.getItemId());
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            List<String> stillMissing = checkRequiredMaterials(
                    item.getRequiredMaterialIds(),
                    caseEntity.getSubmittedMaterials()
            );
            caseEntity.setMissingMaterialIds(stillMissing);

            if (stillMissing.isEmpty()) {
                caseEntity.setStatus(CaseStatus.PENDING_ACCEPT);
                log.info("办件 {} 材料补正完成，进入待受理状态", caseEntity.getCaseNumber());
            }
        }

        return caseRepository.save(caseEntity);
    }

    public Case approveCase(String caseId, String approver, ApprovalResult result, String comment) {
        Optional<Case> caseOpt = caseRepository.findById(caseId);
        if (!caseOpt.isPresent()) {
            throw new RuntimeException("办件不存在");
        }
        Case caseEntity = caseOpt.get();

        if (!CaseStatus.IN_APPROVAL.equals(caseEntity.getStatus())) {
            throw new RuntimeException("当前状态不允许审批");
        }

        Optional<Item> itemOpt = itemRepository.findById(caseEntity.getItemId());
        if (!itemOpt.isPresent()) {
            throw new RuntimeException("事项不存在");
        }
        Item item = itemOpt.get();

        ApprovalRecord record = ApprovalRecord.builder()
                .caseId(caseId)
                .nodeOrder(caseEntity.getCurrentNodeOrder())
                .nodeName(caseEntity.getCurrentNodeName())
                .approver(approver)
                .result(result)
                .comment(comment)
                .build();
        approvalRecordRepository.save(record);

        switch (result) {
            case PASS:
                handleApprovalPass(caseEntity, item);
                break;
            case REJECT:
                caseEntity.setStatus(CaseStatus.REJECTED);
                log.info("办件 {} 被驳回", caseEntity.getCaseNumber());
                break;
            case RETURN_TO_SUPPLEMENT:
                caseEntity.setStatus(CaseStatus.PENDING_SUPPLEMENT);
                log.info("办件 {} 退回补正", caseEntity.getCaseNumber());
                break;
            case TRANSFER:
                break;
        }

        return caseRepository.save(caseEntity);
    }

    private void handleApprovalPass(Case caseEntity, Item item) {
        List<ApprovalNode> nodes = item.getApprovalNodes();
        if (nodes == null || nodes.isEmpty()) {
            completeCase(caseEntity);
            return;
        }

        int currentOrder = caseEntity.getCurrentNodeOrder() != null ? caseEntity.getCurrentNodeOrder() : 0;
        Optional<ApprovalNode> nextNodeOpt = nodes.stream()
                .filter(n -> n.getOrder() > currentOrder)
                .min((a, b) -> a.getOrder() - b.getOrder());

        if (nextNodeOpt.isPresent()) {
            ApprovalNode nextNode = nextNodeOpt.get();
            caseEntity.setCurrentNodeOrder(nextNode.getOrder());
            caseEntity.setCurrentNodeName(nextNode.getName());
            log.info("办件 {} 进入下一审批节点: {}", caseEntity.getCaseNumber(), nextNode.getName());
        } else {
            completeCase(caseEntity);
        }
    }

    private void completeCase(Case caseEntity) {
        caseEntity.setStatus(CaseStatus.COMPLETED);
        caseEntity.setCompletedAt(LocalDateTime.now());
        log.info("办件 {} 已办结", caseEntity.getCaseNumber());
    }

    public Case withdrawCase(String caseId, String reason) {
        Optional<Case> caseOpt = caseRepository.findById(caseId);
        if (!caseOpt.isPresent()) {
            throw new RuntimeException("办件不存在");
        }
        Case caseEntity = caseOpt.get();

        List<CaseStatus> allowedStatuses = Arrays.asList(
                CaseStatus.SUBMITTED,
                CaseStatus.PENDING_ACCEPT,
                CaseStatus.PENDING_SUPPLEMENT,
                CaseStatus.ACCEPTED,
                CaseStatus.IN_APPROVAL
        );

        if (!allowedStatuses.contains(caseEntity.getStatus())) {
            throw new RuntimeException("当前状态不允许撤回");
        }

        if (CaseStatus.IN_APPROVAL.equals(caseEntity.getStatus())) {
            List<ApprovalRecord> records = approvalRecordRepository.findByCaseId(caseId);
            if (!records.isEmpty()) {
                throw new RuntimeException("审批已开始，无法撤回");
            }
        }

        caseEntity.setStatus(CaseStatus.WITHDRAWN);
        caseEntity.setWithdrawnAt(LocalDateTime.now());
        if (reason != null && !reason.isEmpty()) {
            caseEntity.setRemark(caseEntity.getRemark() == null ? reason :
                    caseEntity.getRemark() + "; 撤回原因: " + reason);
        }

        log.info("办件 {} 已撤回", caseEntity.getCaseNumber());
        return caseRepository.save(caseEntity);
    }

    public List<Material> getRequiredMaterialsForItem(String itemId) {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            return new ArrayList<>();
        }
        Item item = itemOpt.get();
        return materialRepository.findByIds(item.getRequiredMaterialIds());
    }

    public List<Material> getOptionalMaterialsForItem(String itemId) {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            return new ArrayList<>();
        }
        Item item = itemOpt.get();
        return materialRepository.findByIds(item.getOptionalMaterialIds());
    }
}

package com.gov.workflow.service;

import com.gov.workflow.entity.Case;
import com.gov.workflow.entity.Item;
import com.gov.workflow.entity.SupervisionRecord;
import com.gov.workflow.enums.CaseStatus;
import com.gov.workflow.repository.CaseRepository;
import com.gov.workflow.repository.ItemRepository;
import com.gov.workflow.repository.SupervisionRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SupervisionService {

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SupervisionRecordRepository supervisionRecordRepository;

    @Scheduled(cron = "0 0 8,12,16,20 * * ?")
    public void checkOverdueCases() {
        log.info("开始执行超期办件检查定时任务...");

        List<Case> overdueCases = caseRepository.findOverdueCases();

        for (Case caseEntity : overdueCases) {
            if (!caseEntity.isOverdue()) {
                caseEntity.setOverdue(true);
                caseEntity.setStatus(CaseStatus.OVERDUE);
                caseRepository.save(caseEntity);
                log.info("办件 {} 已超期，标记为超期状态", caseEntity.getCaseNumber());
            }

            createSupervisionRecord(caseEntity);
        }

        log.info("超期办件检查定时任务完成，共处理 {} 个超期办件", overdueCases.size());
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkWarningCases() {
        log.info("开始执行办件预警提醒定时任务...");

        List<Item> items = itemRepository.findAll();
        for (Item item : items) {
            int warningDays = item.getWarningDays() != null ? item.getWarningDays() : 3;
            List<Case> warningCases = caseRepository.findWarningCases(warningDays);

            for (Case caseEntity : warningCases) {
                if (!caseEntity.isWarningSent()) {
                    caseEntity.setWarningSent(true);
                    caseRepository.save(caseEntity);
                    log.warn("办件 {} 即将超期，提醒经办人: {}。到期时间: {}",
                            caseEntity.getCaseNumber(),
                            caseEntity.getHandler(),
                            caseEntity.getDueDate());
                }
            }
        }

        log.info("办件预警提醒定时任务完成");
    }

    private void createSupervisionRecord(Case caseEntity) {
        LocalDateTime now = LocalDateTime.now();
        Duration overdueDuration = Duration.between(caseEntity.getDueDate(), now);
        long overdueHours = overdueDuration.toHours();

        SupervisionRecord record = SupervisionRecord.builder()
                .caseId(caseEntity.getId())
                .caseNumber(caseEntity.getCaseNumber())
                .itemName(caseEntity.getItemName())
                .applicantName(caseEntity.getApplicantName())
                .handler(caseEntity.getHandler())
                .dueDate(caseEntity.getDueDate())
                .supervisionTime(now)
                .overdueHours(overdueHours)
                .status("待处理")
                .remark("办件已超期" + overdueHours + "小时")
                .build();

        supervisionRecordRepository.save(record);
        log.info("为办件 {} 生成督办记录", caseEntity.getCaseNumber());
    }

    public List<SupervisionRecord> findAllSupervisionRecords() {
        return supervisionRecordRepository.findAll();
    }

    public List<SupervisionRecord> findSupervisionRecordsByCaseId(String caseId) {
        return supervisionRecordRepository.findByCaseId(caseId);
    }

    public List<Case> findOverdueCases() {
        return caseRepository.findOverdueCases();
    }

    public Case forceOverdue(String caseId) {
        Optional<Case> caseOpt = caseRepository.findById(caseId);
        if (!caseOpt.isPresent()) {
            throw new RuntimeException("办件不存在");
        }
        Case caseEntity = caseOpt.get();

        List<CaseStatus> completedStatuses = Arrays.asList(
                CaseStatus.COMPLETED,
                CaseStatus.WITHDRAWN,
                CaseStatus.REJECTED
        );
        if (completedStatuses.contains(caseEntity.getStatus())) {
            throw new RuntimeException("该办件已完成/撤回/驳回，无法超期");
        }

        caseEntity.setDueDate(LocalDateTime.now().minusDays(1));
        caseEntity.setStatus(CaseStatus.OVERDUE);
        caseEntity.setOverdue(true);

        caseRepository.save(caseEntity);

        createSupervisionRecord(caseEntity);

        log.info("办件 {} 已手动标记为超期", caseEntity.getCaseNumber());

        return caseEntity;
    }
}

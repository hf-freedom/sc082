package com.gov.workflow.repository;

import com.gov.workflow.entity.Case;
import com.gov.workflow.enums.CaseStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CaseRepository {
    private final Map<String, Case> cases = new ConcurrentHashMap<>();
    private final AtomicLong caseNumberCounter = new AtomicLong(10000);

    public String generateCaseNumber() {
        String datePart = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
                .format(LocalDate.now());
        long seq = caseNumberCounter.incrementAndGet();
        return "CASE" + datePart + String.format("%05d", seq);
    }

    public Case save(Case caseEntity) {
        if (caseEntity.getId() == null) {
            caseEntity.setId(UUID.randomUUID().toString());
        }
        if (caseEntity.getCaseNumber() == null) {
            caseEntity.setCaseNumber(generateCaseNumber());
        }
        if (caseEntity.getCreatedAt() == null) {
            caseEntity.setCreatedAt(LocalDateTime.now());
        }
        caseEntity.setUpdatedAt(LocalDateTime.now());
        cases.put(caseEntity.getId(), caseEntity);
        return caseEntity;
    }

    public Optional<Case> findById(String id) {
        return Optional.ofNullable(cases.get(id));
    }

    public Optional<Case> findByCaseNumber(String caseNumber) {
        return cases.values().stream()
                .filter(c -> caseNumber.equals(c.getCaseNumber()))
                .findFirst();
    }

    public List<Case> findAll() {
        return new ArrayList<>(cases.values());
    }

    public List<Case> findByStatus(CaseStatus status) {
        return cases.values().stream()
                .filter(c -> status.equals(c.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Case> findByStatuses(List<CaseStatus> statuses) {
        return cases.values().stream()
                .filter(c -> statuses.contains(c.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Case> findByItemId(String itemId) {
        return cases.values().stream()
                .filter(c -> itemId.equals(c.getItemId()))
                .collect(Collectors.toList());
    }

    public List<Case> findByApplicantIdCard(String idCard) {
        return cases.values().stream()
                .filter(c -> idCard.equals(c.getApplicantIdCard()))
                .collect(Collectors.toList());
    }

    public List<Case> findUncompletedByItemIdAndApplicant(String itemId, String applicantIdCard) {
        List<CaseStatus> completedStatuses = Arrays.asList(
                CaseStatus.COMPLETED,
                CaseStatus.WITHDRAWN,
                CaseStatus.REJECTED
        );
        return cases.values().stream()
                .filter(c -> itemId.equals(c.getItemId()))
                .filter(c -> applicantIdCard.equals(c.getApplicantIdCard()))
                .filter(c -> !completedStatuses.contains(c.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Case> findOverdueCases() {
        LocalDateTime now = LocalDateTime.now();
        return cases.values().stream()
                .filter(c -> c.getDueDate() != null)
                .filter(c -> c.getDueDate().isBefore(now))
                .filter(c -> !CaseStatus.COMPLETED.equals(c.getStatus()))
                .filter(c -> !CaseStatus.WITHDRAWN.equals(c.getStatus()))
                .filter(c -> !CaseStatus.REJECTED.equals(c.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Case> findWarningCases(int warningDays) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningDate = now.plusDays(warningDays);
        return cases.values().stream()
                .filter(c -> c.getDueDate() != null)
                .filter(c -> c.getDueDate().isAfter(now))
                .filter(c -> c.getDueDate().isBefore(warningDate) || c.getDueDate().isEqual(warningDate))
                .filter(c -> !c.isWarningSent())
                .filter(c -> !CaseStatus.COMPLETED.equals(c.getStatus()))
                .filter(c -> !CaseStatus.WITHDRAWN.equals(c.getStatus()))
                .filter(c -> !CaseStatus.REJECTED.equals(c.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Case> findByCreatedDate(LocalDate date) {
        return cases.values().stream()
                .filter(c -> c.getCreatedAt() != null)
                .filter(c -> c.getCreatedAt().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Case> findByAcceptedDate(LocalDate date) {
        return cases.values().stream()
                .filter(c -> c.getAcceptedAt() != null)
                .filter(c -> c.getAcceptedAt().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Case> findByCompletedDate(LocalDate date) {
        return cases.values().stream()
                .filter(c -> c.getCompletedAt() != null)
                .filter(c -> c.getCompletedAt().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        cases.remove(id);
    }
}

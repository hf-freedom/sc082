package com.gov.workflow.repository;

import com.gov.workflow.entity.ApprovalRecord;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ApprovalRecordRepository {
    private final Map<String, ApprovalRecord> records = new ConcurrentHashMap<>();

    public ApprovalRecord save(ApprovalRecord record) {
        if (record.getId() == null) {
            record.setId(UUID.randomUUID().toString());
        }
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
        if (record.getApprovedAt() == null) {
            record.setApprovedAt(LocalDateTime.now());
        }
        records.put(record.getId(), record);
        return record;
    }

    public Optional<ApprovalRecord> findById(String id) {
        return Optional.ofNullable(records.get(id));
    }

    public List<ApprovalRecord> findAll() {
        return new ArrayList<>(records.values());
    }

    public List<ApprovalRecord> findByCaseId(String caseId) {
        return records.values().stream()
                .filter(r -> caseId.equals(r.getCaseId()))
                .sorted(Comparator.comparing(ApprovalRecord::getApprovedAt))
                .collect(Collectors.toList());
    }

    public List<ApprovalRecord> findByCaseIdAndNodeOrder(String caseId, Integer nodeOrder) {
        return records.values().stream()
                .filter(r -> caseId.equals(r.getCaseId()))
                .filter(r -> nodeOrder.equals(r.getNodeOrder()))
                .sorted(Comparator.comparing(ApprovalRecord::getApprovedAt))
                .collect(Collectors.toList());
    }

    public Optional<ApprovalRecord> findLatestByCaseId(String caseId) {
        return records.values().stream()
                .filter(r -> caseId.equals(r.getCaseId()))
                .max(Comparator.comparing(ApprovalRecord::getApprovedAt));
    }

    public void deleteById(String id) {
        records.remove(id);
    }
}

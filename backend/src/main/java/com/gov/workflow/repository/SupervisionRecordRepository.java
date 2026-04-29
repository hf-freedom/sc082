package com.gov.workflow.repository;

import com.gov.workflow.entity.SupervisionRecord;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class SupervisionRecordRepository {
    private final Map<String, SupervisionRecord> records = new ConcurrentHashMap<>();

    public SupervisionRecord save(SupervisionRecord record) {
        if (record.getId() == null) {
            record.setId(UUID.randomUUID().toString());
        }
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
        records.put(record.getId(), record);
        return record;
    }

    public Optional<SupervisionRecord> findById(String id) {
        return Optional.ofNullable(records.get(id));
    }

    public List<SupervisionRecord> findAll() {
        return new ArrayList<>(records.values());
    }

    public List<SupervisionRecord> findByCaseId(String caseId) {
        return records.values().stream()
                .filter(r -> caseId.equals(r.getCaseId()))
                .collect(Collectors.toList());
    }

    public List<SupervisionRecord> findByCreatedDate(LocalDate date) {
        return records.values().stream()
                .filter(r -> r.getCreatedAt() != null)
                .filter(r -> r.getCreatedAt().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<SupervisionRecord> findByStatus(String status) {
        return records.values().stream()
                .filter(r -> status.equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        records.remove(id);
    }
}

package com.gov.workflow.repository;

import com.gov.workflow.entity.ResultDocument;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ResultDocumentRepository {
    private final Map<String, ResultDocument> documents = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicLong docNumberCounter = new java.util.concurrent.atomic.AtomicLong(1000);

    public String generateDocumentNumber(String type) {
        String prefix = "DOC";
        if ("ELICENSE".equals(type)) {
            prefix = "LIC";
        }
        String datePart = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
                .format(java.time.LocalDate.now());
        long seq = docNumberCounter.incrementAndGet();
        return prefix + datePart + String.format("%04d", seq);
    }

    public ResultDocument save(ResultDocument document) {
        if (document.getId() == null) {
            document.setId(UUID.randomUUID().toString());
        }
        if (document.getDocumentNumber() == null) {
            document.setDocumentNumber(generateDocumentNumber(document.getDocumentType()));
        }
        if (document.getCreatedAt() == null) {
            document.setCreatedAt(LocalDateTime.now());
        }
        if (document.getIssuedAt() == null) {
            document.setIssuedAt(LocalDateTime.now());
        }
        documents.put(document.getId(), document);
        return document;
    }

    public Optional<ResultDocument> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    public Optional<ResultDocument> findByDocumentNumber(String documentNumber) {
        return documents.values().stream()
                .filter(d -> documentNumber.equals(d.getDocumentNumber()))
                .findFirst();
    }

    public Optional<ResultDocument> findByCaseId(String caseId) {
        return documents.values().stream()
                .filter(d -> caseId.equals(d.getCaseId()))
                .findFirst();
    }

    public List<ResultDocument> findAll() {
        return new ArrayList<>(documents.values());
    }

    public List<ResultDocument> findByDocumentType(String documentType) {
        return documents.values().stream()
                .filter(d -> documentType.equals(d.getDocumentType()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        documents.remove(id);
    }
}

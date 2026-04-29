package com.gov.workflow.repository;

import com.gov.workflow.entity.DailyReport;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class DailyReportRepository {
    private final Map<String, DailyReport> reports = new ConcurrentHashMap<>();

    public DailyReport save(DailyReport report) {
        if (report.getId() == null) {
            report.setId(UUID.randomUUID().toString());
        }
        if (report.getCreatedAt() == null) {
            report.setCreatedAt(java.time.LocalDateTime.now());
        }
        reports.put(report.getId(), report);
        return report;
    }

    public Optional<DailyReport> findById(String id) {
        return Optional.ofNullable(reports.get(id));
    }

    public Optional<DailyReport> findByReportDate(LocalDate reportDate) {
        return reports.values().stream()
                .filter(r -> reportDate.equals(r.getReportDate()))
                .findFirst();
    }

    public List<DailyReport> findAll() {
        return new ArrayList<>(reports.values());
    }

    public List<DailyReport> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return reports.values().stream()
                .filter(r -> !r.getReportDate().isBefore(startDate))
                .filter(r -> !r.getReportDate().isAfter(endDate))
                .sorted((r1, r2) -> r2.getReportDate().compareTo(r1.getReportDate()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        reports.remove(id);
    }
}

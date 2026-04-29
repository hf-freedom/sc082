package com.gov.workflow.service;

import com.gov.workflow.entity.Case;
import com.gov.workflow.entity.DailyReport;
import com.gov.workflow.entity.Item;
import com.gov.workflow.entity.ItemReport;
import com.gov.workflow.enums.CaseStatus;
import com.gov.workflow.repository.CaseRepository;
import com.gov.workflow.repository.DailyReportRepository;
import com.gov.workflow.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReportService {

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Scheduled(cron = "0 0 23 * * ?")
    public void generateDailyReport() {
        log.info("开始生成日报表...");
        LocalDate today = LocalDate.now();
        generateReportForDate(today);
        log.info("日报表生成完成");
    }

    public DailyReport generateReportForDate(LocalDate date) {
        Optional<DailyReport> existingReport = dailyReportRepository.findByReportDate(date);
        if (existingReport.isPresent()) {
            dailyReportRepository.deleteById(existingReport.get().getId());
        }

        List<Case> allCases = caseRepository.findAll();

        int totalCases = allCases.size();

        List<CaseStatus> acceptedStatuses = Arrays.asList(
                CaseStatus.ACCEPTED,
                CaseStatus.IN_APPROVAL,
                CaseStatus.APPROVED,
                CaseStatus.COMPLETED,
                CaseStatus.OVERDUE
        );
        long acceptedCount = allCases.stream()
                .filter(c -> acceptedStatuses.contains(c.getStatus()))
                .count();

        long completedCount = allCases.stream()
                .filter(c -> CaseStatus.COMPLETED.equals(c.getStatus()))
                .count();

        long overdueCount = allCases.stream()
                .filter(c -> CaseStatus.OVERDUE.equals(c.getStatus()))
                .count();

        long returnedCount = allCases.stream()
                .filter(c -> CaseStatus.REJECTED.equals(c.getStatus()))
                .count();

        long withdrawnCount = allCases.stream()
                .filter(c -> CaseStatus.WITHDRAWN.equals(c.getStatus()))
                .count();

        double overdueRate = totalCases > 0 ? (double) overdueCount / totalCases * 100 : 0;
        double returnRate = totalCases > 0 ? (double) returnedCount / totalCases * 100 : 0;

        List<ItemReport> itemReports = generateItemReports();

        DailyReport report = DailyReport.builder()
                .reportDate(date)
                .totalCases(totalCases)
                .acceptedCases((int) acceptedCount)
                .completedCases((int) completedCount)
                .overdueCases((int) overdueCount)
                .returnedCases((int) returnedCount)
                .withdrawnCases((int) withdrawnCount)
                .overdueRate(Math.round(overdueRate * 100.0) / 100.0)
                .returnRate(Math.round(returnRate * 100.0) / 100.0)
                .itemReports(itemReports)
                .build();

        log.info("生成日报表: 日期={}, 总办件={}, 已受理={}, 已办结={}, 超期={}, 退回={}, 撤回={}, 超期率={}%, 退回率={}%",
                date, totalCases, acceptedCount, completedCount, overdueCount, returnedCount, withdrawnCount,
                Math.round(overdueRate * 100.0) / 100.0, Math.round(returnRate * 100.0) / 100.0);

        return dailyReportRepository.save(report);
    }

    private List<ItemReport> generateItemReports() {
        List<Item> items = itemRepository.findAll();
        List<ItemReport> itemReports = new ArrayList<>();

        for (Item item : items) {
            List<Case> itemCases = caseRepository.findByItemId(item.getId());

            int totalCount = itemCases.size();
            int completedCount = (int) itemCases.stream()
                    .filter(c -> CaseStatus.COMPLETED.equals(c.getStatus())).count();
            int overdueCount = (int) itemCases.stream()
                    .filter(c -> CaseStatus.OVERDUE.equals(c.getStatus())).count();
            int returnedCount = (int) itemCases.stream()
                    .filter(c -> CaseStatus.REJECTED.equals(c.getStatus())).count();

            double itemOverdueRate = totalCount > 0 ? (double) overdueCount / totalCount * 100 : 0;
            double itemReturnRate = totalCount > 0 ? (double) returnedCount / totalCount * 100 : 0;

            ItemReport itemReport = ItemReport.builder()
                    .itemId(item.getId())
                    .itemName(item.getName())
                    .totalCount(totalCount)
                    .completedCount(completedCount)
                    .overdueCount(overdueCount)
                    .returnedCount(returnedCount)
                    .overdueRate(Math.round(itemOverdueRate * 100.0) / 100.0)
                    .returnRate(Math.round(itemReturnRate * 100.0) / 100.0)
                    .build();

            itemReports.add(itemReport);
        }

        return itemReports;
    }

    public List<DailyReport> findAllReports() {
        return dailyReportRepository.findAll();
    }

    public Optional<DailyReport> findByReportDate(LocalDate date) {
        return dailyReportRepository.findByReportDate(date);
    }

    public List<DailyReport> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return dailyReportRepository.findByDateRange(startDate, endDate);
    }
}

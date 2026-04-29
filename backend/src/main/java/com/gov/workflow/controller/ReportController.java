package com.gov.workflow.controller;

import com.gov.workflow.common.Result;
import com.gov.workflow.entity.DailyReport;
import com.gov.workflow.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public Result<List<DailyReport>> listAll() {
        return Result.success(reportService.findAllReports());
    }

    @GetMapping("/date/{date}")
    public Result<DailyReport> getByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Optional<DailyReport> reportOpt = reportService.findByReportDate(date);
        if (reportOpt.isPresent()) {
            return Result.success(reportOpt.get());
        }
        DailyReport generated = reportService.generateReportForDate(date);
        return Result.success(generated);
    }

    @GetMapping("/range")
    public Result<List<DailyReport>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return Result.success(reportService.findByDateRange(start, end));
    }

    @PostMapping("/generate/{date}")
    public Result<DailyReport> generateReport(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(reportService.generateReportForDate(date));
    }
}

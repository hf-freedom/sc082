package com.gov.workflow.controller;

import com.gov.workflow.common.Result;
import com.gov.workflow.entity.Case;
import com.gov.workflow.entity.SupervisionRecord;
import com.gov.workflow.service.SupervisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supervision")
public class SupervisionController {

    @Autowired
    private SupervisionService supervisionService;

    @GetMapping("/records")
    public Result<List<SupervisionRecord>> listAllRecords() {
        return Result.success(supervisionService.findAllSupervisionRecords());
    }

    @GetMapping("/records/case/{caseId}")
    public Result<List<SupervisionRecord>> listByCaseId(@PathVariable String caseId) {
        return Result.success(supervisionService.findSupervisionRecordsByCaseId(caseId));
    }

    @GetMapping("/overdue")
    public Result<List<Case>> listOverdueCases() {
        return Result.success(supervisionService.findOverdueCases());
    }

    @PostMapping("/check")
    public Result<Void> manualCheck() {
        supervisionService.checkOverdueCases();
        supervisionService.checkWarningCases();
        return Result.success();
    }

    @PostMapping("/force-overdue/{caseId}")
    public Result<Case> forceOverdue(@PathVariable String caseId) {
        Case caseEntity = supervisionService.forceOverdue(caseId);
        return Result.success(caseEntity);
    }
}

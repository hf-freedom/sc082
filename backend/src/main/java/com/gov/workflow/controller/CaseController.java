package com.gov.workflow.controller;

import com.gov.workflow.common.Result;
import com.gov.workflow.entity.Case;
import com.gov.workflow.entity.Material;
import com.gov.workflow.entity.SubmittedMaterial;
import com.gov.workflow.enums.ApprovalResult;
import com.gov.workflow.enums.CaseStatus;
import com.gov.workflow.service.CaseService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cases")
public class CaseController {

    @Autowired
    private CaseService caseService;

    @GetMapping
    public Result<List<Case>> list() {
        return Result.success(caseService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Case> getById(@PathVariable String id) {
        Optional<Case> caseOpt = caseService.findById(id);
        return caseOpt.map(Result::success).orElseGet(() -> Result.error(404, "办件不存在"));
    }

    @GetMapping("/number/{caseNumber}")
    public Result<Case> getByCaseNumber(@PathVariable String caseNumber) {
        Optional<Case> caseOpt = caseService.findByCaseNumber(caseNumber);
        return caseOpt.map(Result::success).orElseGet(() -> Result.error(404, "办件不存在"));
    }

    @GetMapping("/status/{status}")
    public Result<List<Case>> getByStatus(@PathVariable CaseStatus status) {
        return Result.success(caseService.findByStatus(status));
    }

    @PostMapping("/submit")
    public Result<Case> submitCase(@RequestBody Case caseEntity) {
        Case submitted = caseService.submitCase(caseEntity);
        return Result.success(submitted);
    }

    @PostMapping("/{id}/accept")
    public Result<Case> acceptCase(@PathVariable String id, @RequestBody AcceptRequest request) {
        Case accepted = caseService.acceptCase(id, request.getHandler());
        return Result.success(accepted);
    }

    @PostMapping("/{id}/supplement")
    public Result<Case> supplementMaterials(
            @PathVariable String id,
            @RequestBody List<SubmittedMaterial> materials) {
        Case updated = caseService.supplementMaterials(id, materials);
        return Result.success(updated);
    }

    @PostMapping("/{id}/approve")
    public Result<Case> approveCase(@PathVariable String id, @RequestBody ApproveRequest request) {
        Case approved = caseService.approveCase(
                id,
                request.getApprover(),
                request.getResult(),
                request.getComment()
        );
        return Result.success(approved);
    }

    @PostMapping("/{id}/withdraw")
    public Result<Case> withdrawCase(@PathVariable String id, @RequestBody WithdrawRequest request) {
        Case withdrawn = caseService.withdrawCase(id, request.getReason());
        return Result.success(withdrawn);
    }

    @GetMapping("/{itemId}/materials/required")
    public Result<List<Material>> getRequiredMaterials(@PathVariable String itemId) {
        return Result.success(caseService.getRequiredMaterialsForItem(itemId));
    }

    @GetMapping("/{itemId}/materials/optional")
    public Result<List<Material>> getOptionalMaterials(@PathVariable String itemId) {
        return Result.success(caseService.getOptionalMaterialsForItem(itemId));
    }

    @Data
    public static class AcceptRequest {
        private String handler;
    }

    @Data
    public static class ApproveRequest {
        private String approver;
        private ApprovalResult result;
        private String comment;
    }

    @Data
    public static class WithdrawRequest {
        private String reason;
    }
}

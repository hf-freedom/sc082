package com.gov.workflow.controller;

import com.gov.workflow.common.Result;
import com.gov.workflow.entity.ResultDocument;
import com.gov.workflow.service.ResultDocumentService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/documents")
public class ResultDocumentController {

    @Autowired
    private ResultDocumentService resultDocumentService;

    @GetMapping("/{id}")
    public Result<ResultDocument> getById(@PathVariable String id) {
        Optional<ResultDocument> docOpt = resultDocumentService.findById(id);
        return docOpt.map(Result::success).orElseGet(() -> Result.error(404, "文档不存在"));
    }

    @GetMapping("/case/{caseId}")
    public Result<ResultDocument> getByCaseId(@PathVariable String caseId) {
        Optional<ResultDocument> docOpt = resultDocumentService.findByCaseId(caseId);
        return docOpt.map(Result::success).orElseGet(() -> Result.error(404, "文档不存在"));
    }

    @GetMapping("/number/{documentNumber}")
    public Result<ResultDocument> getByNumber(@PathVariable String documentNumber) {
        Optional<ResultDocument> docOpt = resultDocumentService.findByDocumentNumber(documentNumber);
        return docOpt.map(Result::success).orElseGet(() -> Result.error(404, "文档不存在"));
    }

    @PostMapping("/generate")
    public Result<ResultDocument> generateDocument(@RequestBody GenerateDocumentRequest request) {
        ResultDocument document = resultDocumentService.generateResultDocument(
                request.getCaseId(),
                request.getDocumentType(),
                request.getContent()
        );
        return Result.success(document);
    }

    @Data
    public static class GenerateDocumentRequest {
        private String caseId;
        private String documentType;
        private String content;
    }
}

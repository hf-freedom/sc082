package com.gov.workflow.controller;

import com.gov.workflow.common.Result;
import com.gov.workflow.entity.Material;
import com.gov.workflow.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/materials")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @GetMapping
    public Result<List<Material>> list() {
        return Result.success(materialService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Material> getById(@PathVariable String id) {
        Optional<Material> materialOpt = materialService.findById(id);
        return materialOpt.map(Result::success).orElseGet(() -> Result.error(404, "材料不存在"));
    }

    @GetMapping("/required")
    public Result<List<Material>> listRequired() {
        return Result.success(materialService.findRequired());
    }

    @PostMapping
    public Result<Material> create(@RequestBody Material material) {
        return Result.success(materialService.save(material));
    }
}

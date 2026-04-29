package com.gov.workflow.controller;

import com.gov.workflow.common.Result;
import com.gov.workflow.entity.Item;
import com.gov.workflow.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public Result<List<Item>> list() {
        return Result.success(itemService.findAll());
    }

    @GetMapping("/active")
    public Result<List<Item>> listActive() {
        return Result.success(itemService.findActive());
    }

    @GetMapping("/{id}")
    public Result<Item> getById(@PathVariable String id) {
        Optional<Item> itemOpt = itemService.findById(id);
        return itemOpt.map(Result::success).orElseGet(() -> Result.error(404, "事项不存在"));
    }

    @GetMapping("/code/{code}")
    public Result<Item> getByCode(@PathVariable String code) {
        Optional<Item> itemOpt = itemService.findByCode(code);
        return itemOpt.map(Result::success).orElseGet(() -> Result.error(404, "事项不存在"));
    }

    @GetMapping("/category/{category}")
    public Result<List<Item>> getByCategory(@PathVariable String category) {
        return Result.success(itemService.findByCategory(category));
    }

    @PostMapping
    public Result<Item> create(@RequestBody Item item) {
        return Result.success(itemService.save(item));
    }
}

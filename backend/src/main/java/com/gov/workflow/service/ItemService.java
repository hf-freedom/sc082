package com.gov.workflow.service;

import com.gov.workflow.entity.Item;
import com.gov.workflow.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public List<Item> findActive() {
        return itemRepository.findActive();
    }

    public Optional<Item> findById(String id) {
        return itemRepository.findById(id);
    }

    public Optional<Item> findByCode(String code) {
        return itemRepository.findByCode(code);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public List<Item> findByCategory(String category) {
        return itemRepository.findByCategory(category);
    }
}

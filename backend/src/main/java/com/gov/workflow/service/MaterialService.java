package com.gov.workflow.service;

import com.gov.workflow.entity.Material;
import com.gov.workflow.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    public List<Material> findAll() {
        return materialRepository.findAll();
    }

    public Optional<Material> findById(String id) {
        return materialRepository.findById(id);
    }

    public List<Material> findByIds(List<String> ids) {
        return materialRepository.findByIds(ids);
    }

    public List<Material> findRequired() {
        return materialRepository.findRequired();
    }

    public Material save(Material material) {
        return materialRepository.save(material);
    }
}

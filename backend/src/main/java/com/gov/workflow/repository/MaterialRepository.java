package com.gov.workflow.repository;

import com.gov.workflow.entity.Material;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MaterialRepository {
    private final Map<String, Material> materials = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        LocalDateTime now = LocalDateTime.now();

        Material idCard = Material.builder()
                .id(UUID.randomUUID().toString())
                .name("身份证")
                .code("ID_CARD")
                .description("申请人身份证复印件或扫描件")
                .required(true)
                .fileType("jpg,png,pdf")
                .maxSize(10 * 1024 * 1024L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Material householdRegister = Material.builder()
                .id(UUID.randomUUID().toString())
                .name("户口本")
                .code("HOUSEHOLD_REGISTER")
                .description("户口本复印件")
                .required(true)
                .fileType("jpg,png,pdf")
                .maxSize(10 * 1024 * 1024L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Material applicationForm = Material.builder()
                .id(UUID.randomUUID().toString())
                .name("申请表")
                .code("APPLICATION_FORM")
                .description("业务办理申请表")
                .required(true)
                .fileType("pdf,doc,docx")
                .maxSize(20 * 1024 * 1024L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Material proofMaterial = Material.builder()
                .id(UUID.randomUUID().toString())
                .name("证明材料")
                .code("PROOF_MATERIAL")
                .description("相关证明材料")
                .required(false)
                .fileType("jpg,png,pdf")
                .maxSize(50 * 1024 * 1024L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Material photo = Material.builder()
                .id(UUID.randomUUID().toString())
                .name("一寸照片")
                .code("ONE_INCH_PHOTO")
                .description("一寸免冠照片")
                .required(true)
                .fileType("jpg,png")
                .maxSize(5 * 1024 * 1024L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Material incomeProof = Material.builder()
                .id(UUID.randomUUID().toString())
                .name("收入证明")
                .code("INCOME_PROOF")
                .description("收入证明文件")
                .required(false)
                .fileType("pdf,doc,docx")
                .maxSize(10 * 1024 * 1024L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Material propertyProof = Material.builder()
                .id(UUID.randomUUID().toString())
                .name("房产证明")
                .code("PROPERTY_PROOF")
                .description("房产证或购房合同")
                .required(true)
                .fileType("pdf,doc,docx,jpg,png")
                .maxSize(30 * 1024 * 1024L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Material marriageCertificate = Material.builder()
                .id(UUID.randomUUID().toString())
                .name("结婚证")
                .code("MARRIAGE_CERT")
                .description("结婚证复印件")
                .required(false)
                .fileType("jpg,png,pdf")
                .maxSize(10 * 1024 * 1024L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        materials.put(idCard.getId(), idCard);
        materials.put(householdRegister.getId(), householdRegister);
        materials.put(applicationForm.getId(), applicationForm);
        materials.put(proofMaterial.getId(), proofMaterial);
        materials.put(photo.getId(), photo);
        materials.put(incomeProof.getId(), incomeProof);
        materials.put(propertyProof.getId(), propertyProof);
        materials.put(marriageCertificate.getId(), marriageCertificate);
    }

    public Material save(Material material) {
        if (material.getId() == null) {
            material.setId(UUID.randomUUID().toString());
        }
        if (material.getCreatedAt() == null) {
            material.setCreatedAt(LocalDateTime.now());
        }
        material.setUpdatedAt(LocalDateTime.now());
        materials.put(material.getId(), material);
        return material;
    }

    public Optional<Material> findById(String id) {
        return Optional.ofNullable(materials.get(id));
    }

    public List<Material> findAll() {
        return new ArrayList<>(materials.values());
    }

    public List<Material> findByIds(List<String> ids) {
        return materials.values().stream()
                .filter(m -> ids.contains(m.getId()))
                .collect(Collectors.toList());
    }

    public List<Material> findRequired() {
        return materials.values().stream()
                .filter(Material::isRequired)
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        materials.remove(id);
    }
}

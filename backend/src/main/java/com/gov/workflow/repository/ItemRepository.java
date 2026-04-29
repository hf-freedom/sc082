package com.gov.workflow.repository;

import com.gov.workflow.entity.ApprovalNode;
import com.gov.workflow.entity.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ItemRepository {
    private final Map<String, Item> items = new ConcurrentHashMap<>();

    @Autowired
    private MaterialRepository materialRepository;

    @PostConstruct
    public void init() {
        LocalDateTime now = LocalDateTime.now();

        List<ApprovalNode> standardNodes = Arrays.asList(
                ApprovalNode.builder()
                        .order(1)
                        .name("窗口受理")
                        .role("WINDOW_STAFF")
                        .assignees(Arrays.asList("staff1", "staff2"))
                        .autoApprove(false)
                        .build(),
                ApprovalNode.builder()
                        .order(2)
                        .name("初审")
                        .role("REVIEWER")
                        .assignees(Arrays.asList("reviewer1", "reviewer2"))
                        .autoApprove(false)
                        .build(),
                ApprovalNode.builder()
                        .order(3)
                        .name("终审")
                        .role("APPROVER")
                        .assignees(Arrays.asList("approver1"))
                        .autoApprove(false)
                        .build()
        );

        List<ApprovalNode> simpleNodes = Arrays.asList(
                ApprovalNode.builder()
                        .order(1)
                        .name("窗口受理")
                        .role("WINDOW_STAFF")
                        .assignees(Arrays.asList("staff1", "staff2"))
                        .autoApprove(false)
                        .build(),
                ApprovalNode.builder()
                        .order(2)
                        .name("审批")
                        .role("APPROVER")
                        .assignees(Arrays.asList("approver1", "approver2"))
                        .autoApprove(false)
                        .build()
        );

        List<String> materialIds = materialRepository.findAll().stream()
                .map(m -> m.getId())
                .collect(Collectors.toList());

        Item residencePermit = Item.builder()
                .id(UUID.randomUUID().toString())
                .name("居住证办理")
                .code("RESIDENCE_PERMIT")
                .description("外来人员居住证办理业务")
                .category("户籍管理")
                .processingDays(15)
                .warningDays(3)
                .requiredMaterialIds(Arrays.asList(
                        findMaterialByCode("ID_CARD"),
                        findMaterialByCode("HOUSEHOLD_REGISTER"),
                        findMaterialByCode("APPLICATION_FORM")
                ))
                .optionalMaterialIds(Arrays.asList(
                        findMaterialByCode("PROOF_MATERIAL")
                ))
                .approvalNodes(standardNodes)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Item businessLicense = Item.builder()
                .id(UUID.randomUUID().toString())
                .name("营业执照办理")
                .code("BUSINESS_LICENSE")
                .description("工商营业执照注册登记")
                .category("企业服务")
                .processingDays(7)
                .warningDays(2)
                .requiredMaterialIds(Arrays.asList(
                        findMaterialByCode("ID_CARD"),
                        findMaterialByCode("APPLICATION_FORM"),
                        findMaterialByCode("PROPERTY_PROOF")
                ))
                .optionalMaterialIds(Arrays.asList(
                        findMaterialByCode("INCOME_PROOF")
                ))
                .approvalNodes(standardNodes)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Item socialSecurity = Item.builder()
                .id(UUID.randomUUID().toString())
                .name("社保登记")
                .code("SOCIAL_SECURITY")
                .description("社会保险登记业务")
                .category("社会保障")
                .processingDays(5)
                .warningDays(1)
                .requiredMaterialIds(Arrays.asList(
                        findMaterialByCode("ID_CARD"),
                        findMaterialByCode("HOUSEHOLD_REGISTER"),
                        findMaterialByCode("APPLICATION_FORM")
                ))
                .optionalMaterialIds(Arrays.asList(
                        findMaterialByCode("INCOME_PROOF"),
                        findMaterialByCode("MARRIAGE_CERT")
                ))
                .approvalNodes(simpleNodes)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Item realEstate = Item.builder()
                .id(UUID.randomUUID().toString())
                .name("不动产登记")
                .code("REAL_ESTATE_REG")
                .description("不动产权利登记业务")
                .category("不动产")
                .processingDays(30)
                .warningDays(5)
                .requiredMaterialIds(Arrays.asList(
                        findMaterialByCode("ID_CARD"),
                        findMaterialByCode("PROPERTY_PROOF"),
                        findMaterialByCode("APPLICATION_FORM")
                ))
                .optionalMaterialIds(Arrays.asList(
                        findMaterialByCode("MARRIAGE_CERT"),
                        findMaterialByCode("HOUSEHOLD_REGISTER")
                ))
                .approvalNodes(standardNodes)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Item idCardRenewal = Item.builder()
                .id(UUID.randomUUID().toString())
                .name("身份证补办")
                .code("IDCARD_RENEWAL")
                .description("居民身份证补办业务")
                .category("户籍管理")
                .processingDays(3)
                .warningDays(1)
                .requiredMaterialIds(Arrays.asList(
                        findMaterialByCode("HOUSEHOLD_REGISTER"),
                        findMaterialByCode("APPLICATION_FORM"),
                        findMaterialByCode("ONE_INCH_PHOTO")
                ))
                .optionalMaterialIds(Arrays.asList(
                        findMaterialByCode("PROOF_MATERIAL")
                ))
                .approvalNodes(simpleNodes)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        items.put(residencePermit.getId(), residencePermit);
        items.put(businessLicense.getId(), businessLicense);
        items.put(socialSecurity.getId(), socialSecurity);
        items.put(realEstate.getId(), realEstate);
        items.put(idCardRenewal.getId(), idCardRenewal);
    }

    private String findMaterialByCode(String code) {
        return materialRepository.findAll().stream()
                .filter(m -> code.equals(m.getCode()))
                .map(m -> m.getId())
                .findFirst()
                .orElse(UUID.randomUUID().toString());
    }

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(UUID.randomUUID().toString());
        }
        if (item.getCreatedAt() == null) {
            item.setCreatedAt(LocalDateTime.now());
        }
        item.setUpdatedAt(LocalDateTime.now());
        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(String id) {
        return Optional.ofNullable(items.get(id));
    }

    public Optional<Item> findByCode(String code) {
        return items.values().stream()
                .filter(item -> code.equals(item.getCode()))
                .findFirst();
    }

    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    public List<Item> findActive() {
        return items.values().stream()
                .filter(Item::isActive)
                .collect(Collectors.toList());
    }

    public List<Item> findByCategory(String category) {
        return items.values().stream()
                .filter(item -> category.equals(item.getCategory()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        items.remove(id);
    }
}

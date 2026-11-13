package com.cap43.service;

import com.cap43.common.SlaUtil;
import com.cap43.dto.AdminIssueRow;
import com.cap43.dto.TenantIssueView;
import com.cap43.entity.*;
import com.cap43.repository.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class IssueAssembler {

    private final UnitRepository unitRepo;
    private final PropertyRepository propertyRepo;
    private final TenantRepository tenantRepo;
    private final WorkOrderRepository workOrderRepo;
    private final VendorRepository vendorRepo;

    public TenantIssueView tenantView(Issue i, LocalDateTime now) {
        return new TenantIssueView(i.getIssueId(), i.getCategory(), i.getTitle(), i.getDescription(),
                i.getSeverity(), i.getStatus(), i.getCreatedAt(), i.getSlaDueAt(), i.getResolvedAt(),
                SlaUtil.isOverdue(i, now));
    }

    public AdminIssueRow adminRow(Issue i) {
        return adminRows(List.of(i)).get(0);
    }

    public List<AdminIssueRow> adminRows(List<Issue> issues) {
        if (issues.isEmpty()) return List.of();
        LocalDateTime now = LocalDateTime.now();

        Map<Integer, Unit> units = unitRepo
                .findAllById(issues.stream().map(Issue::getUnitId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Unit::getUnitId, Function.identity()));
        Map<Integer, Property> props = propertyRepo
                .findAllById(units.values().stream().map(Unit::getPropertyId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Property::getPropertyId, Function.identity()));
        Map<Integer, Tenant> tenants = tenantRepo
                .findAllById(issues.stream().map(Issue::getTenantId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Tenant::getTenantId, Function.identity()));

        Map<Integer, List<WorkOrder>> wosByIssue = workOrderRepo
                .findByIssueIdIn(issues.stream().map(Issue::getIssueId).toList())
                .stream().collect(Collectors.groupingBy(WorkOrder::getIssueId));
        Set<Integer> vendorIds = wosByIssue.values().stream().flatMap(List::stream)
                .map(WorkOrder::getVendorId).collect(Collectors.toSet());
        Map<Integer, Vendor> vendors = vendorRepo.findAllById(vendorIds)
                .stream().collect(Collectors.toMap(Vendor::getVendorId, Function.identity()));

        return issues.stream().map(i -> {
            Unit u = units.get(i.getUnitId());
            Property p = (u == null) ? null : props.get(u.getPropertyId());
            Tenant t = tenants.get(i.getTenantId());

            String vendorName = wosByIssue.getOrDefault(i.getIssueId(), List.of()).stream()
                    .filter(w -> !"REJECTED".equals(w.getStatus()))
                    .max(Comparator.comparing(WorkOrder::getAssignedAt))
                    .map(w -> vendors.get(w.getVendorId()))
                    .map(Vendor::getCompanyName)
                    .orElse(null);

            return new AdminIssueRow(i.getIssueId(),
                    p == null ? null : p.getName(),
                    u == null ? null : u.getUnitNumber(),
                    t == null ? null : t.getTenantRef(),
                    i.getCategory(), i.getTitle(), i.getSeverity(), i.getSeveritySource(),
                    i.getStatus(), i.getEscalationLevel(), i.getCreatedAt(), i.getSlaDueAt(),
                    i.getResolvedAt(), SlaUtil.isOverdue(i, now), vendorName);
        }).toList();
    }
}

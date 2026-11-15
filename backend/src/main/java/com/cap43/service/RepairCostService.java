package com.cap43.service;

import com.cap43.dto.*;
import com.cap43.entity.RepairCost;
import com.cap43.entity.WorkOrder;
import com.cap43.repository.*;
import com.cap43.security.AuthUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class RepairCostService {

    private final RepairCostRepository costRepo;
    private final WorkOrderRepository workOrderRepo;
    private final IssueRepository issueRepo;
    private final InspectionItemRepository itemRepo;
    private final VendorRepository vendorRepo;
    private final AuditService audit;

    @Transactional
    public CostEntryView create(CreateCostRequest req, AuthUser admin, String ip) {
        Integer issueId = req.issueId();
        Integer itemId = req.inspectionItemId();
        Integer vendorId = req.vendorId();

        if (req.workOrderId() != null) {
            WorkOrder wo = workOrderRepo.findById(req.workOrderId())
                    .orElseThrow(() -> bad("Unknown work order"));
            if (issueId != null && wo.getIssueId() != null && !issueId.equals(wo.getIssueId())) {
                throw bad("Issue does not match the work order");
            }
            if (itemId != null && wo.getInspectionItemId() != null && !itemId.equals(wo.getInspectionItemId())) {
                throw bad("Inspection item does not match the work order");
            }
            
            if (issueId == null) issueId = wo.getIssueId();
            if (itemId == null) itemId = wo.getInspectionItemId();
            if (vendorId == null) vendorId = wo.getVendorId();
        }

        if (issueId == null && req.workOrderId() == null && itemId == null) {
            throw bad("Link the cost to an issue, work order or inspection item");
        }
        if (issueId != null && !issueRepo.existsById(issueId)) throw bad("Unknown issue");
        if (itemId != null && !itemRepo.existsById(itemId)) throw bad("Unknown inspection item");
        if (vendorId != null && !vendorRepo.existsById(vendorId)) throw bad("Unknown vendor");

        RepairCost c = new RepairCost();
        c.setIssueId(issueId);
        c.setWorkOrderId(req.workOrderId());
        c.setInspectionItemId(itemId);
        c.setVendorId(vendorId);
        c.setEntryType("CHARGE");
        c.setAmount(req.amount().setScale(2, RoundingMode.HALF_UP));
        c.setCostType(req.costType());
        c.setDescription(req.description());
        c.setInvoiceRef(req.invoiceRef());
        c.setIncurredAt(req.incurredAt() != null ? req.incurredAt() : LocalDateTime.now());
        c.setEnteredByUserId(admin.userId());
        RepairCost saved = costRepo.save(c);

        audit.log(admin.userId(), "COST_CREATE", "REPAIR_COST", String.valueOf(saved.getCostId()),
                saved.getCostType() + " " + saved.getAmount(), ip);
        return view(saved);
    }

    @Transactional
    public CostEntryView reverse(Integer costId, ReverseCostRequest req, AuthUser admin, String ip) {
        RepairCost original = costRepo.findById(costId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost entry not found"));
        if (!"CHARGE".equals(original.getEntryType())) {
            throw bad("Only charges can be reversed");
        }
        if (costRepo.existsByReversesCostId(costId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This entry has already been reversed");
        }

        RepairCost r = new RepairCost();
        r.setIssueId(original.getIssueId());
        r.setWorkOrderId(original.getWorkOrderId());
        r.setInspectionItemId(original.getInspectionItemId());
        r.setVendorId(original.getVendorId());
        r.setEntryType("ADJUSTMENT");
        r.setAmount(original.getAmount().negate());
        r.setCostType(original.getCostType());
        r.setDescription("Reversal: " + req.reason().trim());
        r.setInvoiceRef(original.getInvoiceRef());
        r.setReversesCostId(costId);
        r.setIncurredAt(LocalDateTime.now());
        r.setEnteredByUserId(admin.userId());
        RepairCost saved = costRepo.save(r);

        audit.log(admin.userId(), "COST_REVERSE", "REPAIR_COST", String.valueOf(costId), req.reason().trim(), ip);
        return view(saved);
    }

    @Transactional(readOnly = true)
    public LedgerView list(Integer issueId, Integer workOrderId, Integer inspectionItemId) {
        List<RepairCost> rows;
        if (workOrderId != null) {
            rows = costRepo.findByWorkOrderIdOrderByCostIdAsc(workOrderId);
        } else if (inspectionItemId != null) {
            rows = costRepo.findByInspectionItemIdOrderByCostIdAsc(inspectionItemId);
        } else if (issueId != null) {
            rows = costRepo.findByIssueIdOrderByCostIdAsc(issueId);
        } else {
            throw bad("Provide issueId, workOrderId or inspectionItemId");
        }
        return new LedgerView(rows.stream().map(this::view).toList(), net(rows));
    }

    
    @Transactional(readOnly = true)
    public BigDecimal netForInspectionItem(Integer inspectionItemId) {
        return net(costRepo.findByInspectionItemIdOrderByCostIdAsc(inspectionItemId));
    }

    private static BigDecimal net(List<RepairCost> rows) {
        return rows.stream().map(RepairCost::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CostEntryView view(RepairCost c) {
        return new CostEntryView(c.getCostId(), c.getIssueId(), c.getWorkOrderId(), c.getInspectionItemId(),
                c.getVendorId(), c.getEntryType(), c.getAmount(), c.getCostType(), c.getDescription(),
                c.getInvoiceRef(), c.getReversesCostId(), c.getIncurredAt(), c.getEnteredByUserId());
    }

    private static ResponseStatusException bad(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}

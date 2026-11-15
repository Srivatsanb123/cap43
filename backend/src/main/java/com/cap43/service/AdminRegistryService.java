package com.cap43.service;

import com.cap43.dto.*;
import com.cap43.entity.*;
import com.cap43.repository.*;
import com.cap43.security.AuthUser;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminRegistryService {

    private final PropertyRepository propertyRepo;
    private final UnitRepository unitRepo;
    private final LeaseRepository leaseRepo;
    private final TenantRepository tenantRepo;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<PropertyView> properties() {
        Map<Integer, Long> counts = unitRepo.findAll().stream()
                .collect(Collectors.groupingBy(Unit::getPropertyId, Collectors.counting()));
        return propertyRepo.findAll().stream()
                .map(p -> propertyView(p, counts.getOrDefault(p.getPropertyId(), 0L))).toList();
    }

    @Transactional
    public PropertyView createProperty(CreatePropertyRequest req, AuthUser admin, String ip) {
        Property p = new Property();
        p.setName(req.name().trim());
        p.setAddressLine(req.addressLine());
        p.setCity(req.city());
        p.setState(req.state());
        p.setPostalCode(req.postalCode());
        Property saved = propertyRepo.save(p);
        audit.log(admin.userId(), "PROPERTY_CREATE", "PROPERTY", String.valueOf(saved.getPropertyId()),
                saved.getName(), ip);
        return propertyView(saved, 0L);
    }

    @Transactional(readOnly = true)
    public List<UnitView> units(Integer propertyId) {
        requireProperty(propertyId);
        Set<Integer> occupied = leaseRepo.findByStatusOrderByStartDateDesc("ACTIVE").stream()
                .map(Lease::getUnitId).collect(Collectors.toSet());
        return unitRepo.findByPropertyIdOrderByUnitNumberAsc(propertyId).stream()
                .map(u -> new UnitView(u.getUnitId(), u.getPropertyId(), u.getUnitNumber(),
                        u.getFloorNo(), u.getBedrooms(), occupied.contains(u.getUnitId())))
                .toList();
    }

    @Transactional
    public UnitView createUnit(Integer propertyId, CreateUnitRequest req, AuthUser admin, String ip) {
        requireProperty(propertyId);
        String number = req.unitNumber().trim();
        if (unitRepo.existsByPropertyIdAndUnitNumber(propertyId, number)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unit number already exists in this property");
        }
        Unit u = new Unit();
        u.setPropertyId(propertyId);
        u.setUnitNumber(number);
        u.setFloorNo(req.floorNo());
        u.setBedrooms(req.bedrooms());
        Unit saved = unitRepo.save(u);
        audit.log(admin.userId(), "UNIT_CREATE", "UNIT", String.valueOf(saved.getUnitId()),
                "property " + propertyId + " unit " + number, ip);
        return new UnitView(saved.getUnitId(), propertyId, number, saved.getFloorNo(), saved.getBedrooms(), false);
    }

    @Transactional(readOnly = true)
    public List<LeaseRow> leases(String status) {
        List<Lease> leases = (status == null || status.isBlank())
                ? leaseRepo.findAllByOrderByStartDateDesc()
                : leaseRepo.findByStatusOrderByStartDateDesc(status.trim());
        return leaseRows(leases);
    }

    @Transactional
    public LeaseRow endLease(Integer leaseId, EndLeaseRequest req, AuthUser admin, String ip) {
        Lease l = leaseRepo.findById(leaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lease not found"));
        if ("ENDED".equals(l.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lease is already ended");
        }
        if (req.moveOutDate().isBefore(l.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Move-out date cannot be before the lease start");
        }
        if (req.moveOutDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Move-out date cannot be in the future");
        }
        l.setStatus("ENDED");
        l.setMoveOutDate(req.moveOutDate());
        leaseRepo.save(l);
        audit.log(admin.userId(), "LEASE_END", "LEASE", String.valueOf(leaseId),
                "move-out " + req.moveOutDate(), ip);
        return leaseRows(List.of(l)).get(0);
    }

    private List<LeaseRow> leaseRows(List<Lease> leases) {
        if (leases.isEmpty()) return List.of();
        Map<Integer, Unit> units = unitRepo
                .findAllById(leases.stream().map(Lease::getUnitId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Unit::getUnitId, Function.identity()));
        Map<Integer, Property> props = propertyRepo
                .findAllById(units.values().stream().map(Unit::getPropertyId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Property::getPropertyId, Function.identity()));
        Map<Integer, Tenant> tenants = tenantRepo
                .findAllById(leases.stream().map(Lease::getTenantId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Tenant::getTenantId, Function.identity()));

        return leases.stream().map(l -> {
            Unit u = units.get(l.getUnitId());
            Property p = u == null ? null : props.get(u.getPropertyId());
            Tenant t = tenants.get(l.getTenantId());
            return new LeaseRow(l.getLeaseId(), p == null ? null : p.getName(),
                    u == null ? null : u.getUnitNumber(), t == null ? null : t.getTenantRef(),
                    l.getStartDate(), l.getEndDate(), l.getMonthlyRent(), l.getDepositAmount(),
                    l.getStatus(), l.getMoveOutDate());
        }).toList();
    }

    private PropertyView propertyView(Property p, long unitCount) {
        return new PropertyView(p.getPropertyId(), p.getName(), p.getAddressLine(), p.getCity(),
                p.getState(), p.getPostalCode(), unitCount);
    }

    private void requireProperty(Integer id) {
        if (!propertyRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }
    }
}

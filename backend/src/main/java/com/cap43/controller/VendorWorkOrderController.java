package com.cap43.controller;

import com.cap43.dto.VendorWorkOrderView;
import com.cap43.dto.WorkOrderUpdateRequest;
import com.cap43.security.AuthUser;
import com.cap43.service.VendorWorkService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/work-orders")
@RequiredArgsConstructor
public class VendorWorkOrderController {

    private final VendorWorkService service;

    @GetMapping
    public List<VendorWorkOrderView> list(@AuthenticationPrincipal AuthUser user) {
        return service.list(user);
    }

    @PostMapping("/{id}/updates")
    public VendorWorkOrderView update(@AuthenticationPrincipal AuthUser user, @PathVariable Integer id,
                                      @Valid @RequestBody WorkOrderUpdateRequest req) {
        return service.update(user, id, req);
    }
}

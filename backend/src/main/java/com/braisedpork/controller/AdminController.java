package com.braisedpork.controller;

import com.braisedpork.dto.AdminStatsResponse;
import com.braisedpork.dto.OrderResponse;
import com.braisedpork.dto.Result;
import com.braisedpork.dto.UserSummaryDTO;
import com.braisedpork.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/orders")
    public ResponseEntity<Result<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        int pageIndex = Math.max(0, page - 1);
        Page<OrderResponse> orders = adminService.getAllOrders(pageIndex, size, status);
        return ResponseEntity.ok(Result.success(orders));
    }

    @PutMapping("/orders/{id}/payment/approve")
    public ResponseEntity<Result<OrderResponse>> approvePayment(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(adminService.approvePayment(id)));
    }

    @PutMapping("/orders/{id}/payment/reject")
    public ResponseEntity<Result<OrderResponse>> rejectPayment(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(adminService.rejectPayment(id)));
    }

    @GetMapping("/users")
    public ResponseEntity<Result<Page<UserSummaryDTO>>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int pageIndex = Math.max(0, page - 1);
        Page<UserSummaryDTO> users = adminService.getUserList(pageIndex, size);
        return ResponseEntity.ok(Result.success(users));
    }

    @GetMapping("/stats")
    public ResponseEntity<Result<AdminStatsResponse>> getStats() {
        AdminStatsResponse stats = adminService.getStats();
        return ResponseEntity.ok(Result.success(stats));
    }
}

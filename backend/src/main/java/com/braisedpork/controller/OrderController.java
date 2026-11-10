package com.braisedpork.controller;

import com.braisedpork.dto.OrderCreateRequest;
import com.braisedpork.dto.OrderResponse;
import com.braisedpork.dto.Result;
import com.braisedpork.security.JwtUtils;
import com.braisedpork.exception.UnauthorizedException;
import com.braisedpork.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<Result<OrderResponse>> createOrder(
            HttpServletRequest request,
            @Valid @RequestBody OrderCreateRequest orderRequest) {
        Long userId = extractUserId(request);
        OrderResponse order = orderService.createOrder(userId, orderRequest);
        return ResponseEntity.ok(Result.success(order));
    }

    @GetMapping
    public ResponseEntity<Result<List<OrderResponse>>> getOrderList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = extractUserId(request);
        int pageIndex = Math.max(0, page - 1);
        List<OrderResponse> orders = orderService.getOrderList(userId, pageIndex, size);
        return ResponseEntity.ok(Result.success(orders));
    }

    @GetMapping("/counts")
    public ResponseEntity<Result<Map<String, Long>>> getOrderCounts(HttpServletRequest request) {
        return ResponseEntity.ok(Result.success(orderService.getOrderCounts(extractUserId(request))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<OrderResponse>> getOrderDetail(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = extractUserId(request);
        OrderResponse order = orderService.getOrderDetail(userId, id);
        return ResponseEntity.ok(Result.success(order));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Result<OrderResponse>> cancelOrder(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = extractUserId(request);
        OrderResponse order = orderService.cancelOrder(userId, id);
        return ResponseEntity.ok(Result.success(order));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<Result<OrderResponse>> payOrder(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = extractUserId(request);
        OrderResponse order = orderService.payOrder(userId, id);
        return ResponseEntity.ok(Result.success(order));
    }

    @PutMapping("/{id}/ship")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<OrderResponse>> shipOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String trackingNo) {
        OrderResponse order = orderService.shipOrder(id, trackingNo);
        return ResponseEntity.ok(Result.success(order));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Result<OrderResponse>> confirmReceive(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = extractUserId(request);
        OrderResponse order = orderService.confirmReceive(userId, id);
        return ResponseEntity.ok(Result.success(order));
    }

    private Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("登录状态无效或已过期");
        }
        String token = authHeader.substring(7);
        return jwtUtils.getUserIdFromToken(token);
    }
}

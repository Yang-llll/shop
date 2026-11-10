package com.braisedpork.service.impl;

import com.braisedpork.dto.AdminStatsResponse;
import com.braisedpork.dto.OrderResponse;
import com.braisedpork.dto.UserSummaryDTO;
import com.braisedpork.exception.ConflictException;
import com.braisedpork.exception.ResourceNotFoundException;
import com.braisedpork.entity.Order;
import com.braisedpork.entity.OrderItem;
import com.braisedpork.entity.User;
import com.braisedpork.repository.OrderItemRepository;
import com.braisedpork.repository.OrderRepository;
import com.braisedpork.repository.ProductRepository;
import com.braisedpork.repository.UserRepository;
import com.braisedpork.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    public Page<OrderResponse> getAllOrders(int page, int size, String status) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders;

        if (status != null && !status.isEmpty()) {
            try {
                orders = orderRepository.findByStatus(Order.Status.valueOf(status), pageable);
            } catch (IllegalArgumentException e) {
                throw new ConflictException("无效的订单状态：" + status);
            }
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            return convertToResponse(order, items);
        });
    }

    @Override
    @Transactional
    public OrderResponse approvePayment(Long orderId) {
        return reviewPayment(orderId, Order.Status.PAID);
    }

    @Override
    @Transactional
    public OrderResponse rejectPayment(Long orderId) {
        return reviewPayment(orderId, Order.Status.PENDING);
    }

    private OrderResponse reviewPayment(Long orderId, Order.Status targetStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        if (order.getStatus() != Order.Status.PAYMENT_REVIEW) {
            throw new ConflictException("只能审核待确认支付的订单");
        }

        order.setStatus(targetStatus);
        order = orderRepository.save(order);
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return convertToResponse(order, items);
    }

    @Override
    public Page<UserSummaryDTO> getUserList(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).map(user -> UserSummaryDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .role(user.getRole().name())
                .createTime(user.getCreateTime())
                .build());
    }

    @Override
    public AdminStatsResponse getStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalOrders(orderRepository.count())
                .totalSales(orderRepository.sumTotalSales())
                .totalProducts(productRepository.count())
                .build();
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new com.braisedpork.exception.BusinessException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "分页参数不合法");
        }
    }

    private OrderResponse convertToResponse(Order order, List<OrderItem> items) {
        String username = userRepository.findById(order.getUserId())
                .map(User::getUsername)
                .orElse("-");

        List<OrderResponse.OrderItemResponse> itemResponses = items.stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productPrice(item.getProductPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .username(username)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .address(order.getAddress())
                .phone(order.getPhone())
                .receiver(order.getReceiver())
                .items(itemResponses)
                .createTime(order.getCreateTime())
                .updateTime(order.getUpdateTime())
                .shippingTime(order.getShippingTime())
                .trackingNo(order.getTrackingNo())
                .build();
    }
}

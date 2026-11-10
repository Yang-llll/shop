package com.braisedpork.service.impl;

import com.braisedpork.dto.OrderCreateRequest;
import com.braisedpork.dto.OrderResponse;
import com.braisedpork.entity.Order;
import com.braisedpork.entity.OrderItem;
import com.braisedpork.entity.Product;
import com.braisedpork.exception.ConflictException;
import com.braisedpork.exception.ForbiddenOperationException;
import com.braisedpork.exception.ResourceNotFoundException;
import com.braisedpork.repository.OrderItemRepository;
import com.braisedpork.repository.OrderRepository;
import com.braisedpork.repository.ProductRepository;
import com.braisedpork.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, OrderCreateRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = Order.builder()
                .orderNo(generateOrderNo())
                .userId(userId)
                .totalAmount(BigDecimal.ZERO)
                .status(Order.Status.PENDING)
                .address(request.getAddress())
                .phone(request.getPhone())
                .receiver(request.getReceiver())
                .build();

        order = orderRepository.save(order);

        for (OrderCreateRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findByIdForUpdate(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("商品不存在：" + itemRequest.getProductId()));

            if (product.getStatus() != Product.Status.ON_SALE) {
                throw new ConflictException("商品已下架：" + product.getName());
            }
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new ConflictException("商品库存不足：" + product.getName());
            }

            product.setStock(product.getStock() - itemRequest.getQuantity());
            product.setSalesCount(product.getSalesCount() + itemRequest.getQuantity());
            productRepository.save(product);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .build();

            orderItemRepository.save(orderItem);
        }

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        return getOrderDetail(userId, order.getId());
    }

    @Override
    public List<OrderResponse> getOrderList(Long userId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new com.braisedpork.exception.BusinessException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "分页参数不合法");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);

        return orders.getContent().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return convertToResponse(order, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("无权查看该订单");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return convertToResponse(order, items);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("无权操作该订单");
        }

        if (order.getStatus() != Order.Status.PENDING) {
            throw new ConflictException("只有待支付订单可以取消");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            Product product = productRepository.findByIdForUpdate(item.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                long currentSales = product.getSalesCount() == null ? 0L : product.getSalesCount();
                product.setSalesCount(Math.max(0L, currentSales - item.getQuantity()));
                productRepository.save(product);
            }
        }

        order.setStatus(Order.Status.CANCELLED);
        orderRepository.save(order);

        return convertToResponse(order, items);
    }

    @Override
    @Transactional
    public OrderResponse payOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("无权操作该订单");
        }
        if (order.getStatus() != Order.Status.PENDING) {
            throw new ConflictException("只有待支付订单可以提交支付确认");
        }
        order.setStatus(Order.Status.PAYMENT_REVIEW);
        orderRepository.save(order);
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return convertToResponse(order, items);
    }

    @Override
    @Transactional
    public OrderResponse shipOrder(Long orderId, String trackingNo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        if (order.getStatus() != Order.Status.PAID) {
            throw new ConflictException("只有待发货订单可以发货");
        }

        order.setStatus(Order.Status.SHIPPED);
        order.setShippingTime(LocalDateTime.now());
        if (trackingNo != null && !trackingNo.isEmpty()) {
            order.setTrackingNo(trackingNo);
        }
        order = orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return convertToResponse(order, items);
    }

    @Override
    @Transactional
    public OrderResponse confirmReceive(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("无权操作该订单");
        }

        if (order.getStatus() != Order.Status.SHIPPED) {
            throw new ConflictException("只有已发货订单可以确认收货");
        }

        order.setStatus(Order.Status.COMPLETED);
        order = orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return convertToResponse(order, items);
    }

    @Override
    public Map<String, Long> getOrderCounts(Long userId) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("all", orderRepository.countByUserId(userId));
        counts.put("pending", orderRepository.countByUserIdAndStatus(userId, Order.Status.PENDING));
        counts.put("paymentReview", orderRepository.countByUserIdAndStatus(userId, Order.Status.PAYMENT_REVIEW));
        counts.put("paid", orderRepository.countByUserIdAndStatus(userId, Order.Status.PAID));
        counts.put("shipped", orderRepository.countByUserIdAndStatus(userId, Order.Status.SHIPPED));
        counts.put("completed", orderRepository.countByUserIdAndStatus(userId, Order.Status.COMPLETED));
        return counts;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "ORD" + timestamp + uuid;
    }

    private OrderResponse convertToResponse(Order order, List<OrderItem> items) {
        List<OrderResponse.OrderItemResponse> itemResponses = items.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    return OrderResponse.OrderItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .productImage(product != null ? product.getImageUrl() : null)
                            .productPrice(item.getProductPrice())
                            .quantity(item.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
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

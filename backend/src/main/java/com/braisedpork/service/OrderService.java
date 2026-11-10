package com.braisedpork.service;

import com.braisedpork.dto.OrderCreateRequest;
import com.braisedpork.dto.OrderResponse;

import java.util.List;
import java.util.Map;

public interface OrderService {

    OrderResponse createOrder(Long userId, OrderCreateRequest request);

    List<OrderResponse> getOrderList(Long userId, int page, int size);

    OrderResponse getOrderDetail(Long userId, Long orderId);

    OrderResponse cancelOrder(Long userId, Long orderId);

    OrderResponse payOrder(Long userId, Long orderId);

    OrderResponse shipOrder(Long orderId, String trackingNo);

    OrderResponse confirmReceive(Long userId, Long orderId);

    Map<String, Long> getOrderCounts(Long userId);
}

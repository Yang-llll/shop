package com.braisedpork.service;

import com.braisedpork.dto.AdminStatsResponse;
import com.braisedpork.dto.OrderResponse;
import com.braisedpork.dto.UserSummaryDTO;
import org.springframework.data.domain.Page;

public interface AdminService {

    Page<OrderResponse> getAllOrders(int page, int size, String status);

    OrderResponse approvePayment(Long orderId);

    OrderResponse rejectPayment(Long orderId);

    Page<UserSummaryDTO> getUserList(int page, int size);

    AdminStatsResponse getStats();
}

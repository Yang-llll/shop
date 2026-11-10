package com.braisedpork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsResponse {

    private Long totalUsers;
    private Long totalOrders;
    private BigDecimal totalSales;
    private Long totalProducts;
}

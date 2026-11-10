package com.braisedpork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String orderNo;
    private String username;
    private BigDecimal totalAmount;
    private String status;
    private String address;
    private String phone;
    private String receiver;
    private List<OrderItemResponse> items;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime shippingTime;
    private String trackingNo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal productPrice;
        private Integer quantity;
    }
}

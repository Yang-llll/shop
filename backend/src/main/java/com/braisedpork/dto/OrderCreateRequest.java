package com.braisedpork.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

    @NotBlank(message = "收货地址不能为空")
    private String address;

    @NotBlank(message = "收货人手机号不能为空")
    private String phone;

    @NotBlank(message = "收货人姓名不能为空")
    private String receiver;

    @NotEmpty(message = "订单商品不能为空")
    @Size(min = 1, message = "至少需要一件商品")
    @Valid
    private List<OrderItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {

        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "购买数量不能为空")
        @Min(value = 1, message = "购买数量至少为1")
        private Integer quantity;
    }
}

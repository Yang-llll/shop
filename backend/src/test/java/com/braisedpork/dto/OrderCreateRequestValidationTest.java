package com.braisedpork.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderCreateRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validatesNestedOrderItemQuantity() {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .address("湖南省湘潭市雨湖区")
                .phone("13800138000")
                .receiver("杨超")
                .items(List.of(OrderCreateRequest.OrderItemRequest.builder()
                        .productId(1L)
                        .quantity(0)
                        .build()))
                .build();

        Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(violation ->
                violation.getPropertyPath().toString().contains("quantity")
                        && violation.getMessage().contains("至少为1")));
    }
}

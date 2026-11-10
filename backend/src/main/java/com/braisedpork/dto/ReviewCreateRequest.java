package com.braisedpork.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {
    @NotNull
    private Long orderId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 500)
    private String content;
}

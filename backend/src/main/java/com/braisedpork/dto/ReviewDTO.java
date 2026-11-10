package com.braisedpork.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {

    private Long id;
    private Long userId;
    private String username;
    private Long productId;
    private Long orderId;

    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 500)
    private String content;

    private LocalDateTime createTime;
}

package com.braisedpork.controller;

import com.braisedpork.dto.ReviewCreateRequest;
import com.braisedpork.dto.ReviewDTO;
import com.braisedpork.dto.Result;
import com.braisedpork.security.JwtUtils;
import com.braisedpork.exception.UnauthorizedException;
import com.braisedpork.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<Result<ReviewDTO>> createReview(
            HttpServletRequest request,
            @Valid @RequestBody ReviewCreateRequest reviewRequest) {
        Long userId = extractUserId(request);
        ReviewDTO review = reviewService.createReview(userId, reviewRequest);
        return ResponseEntity.ok(Result.success(review));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Result<List<ReviewDTO>>> getProductReviews(@PathVariable Long productId) {
        List<ReviewDTO> reviews = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(Result.success(reviews));
    }

    @GetMapping("/check/{orderId}/product/{productId}")
    public ResponseEntity<Result<Boolean>> hasReviewed(
            HttpServletRequest request,
            @PathVariable Long orderId,
            @PathVariable Long productId) {
        Long userId = extractUserId(request);
        boolean reviewed = reviewService.hasUserReviewed(userId, orderId, productId);
        return ResponseEntity.ok(Result.success(reviewed));
    }

    private Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("登录状态无效或已过期");
        }
        String token = authHeader.substring(7);
        return jwtUtils.getUserIdFromToken(token);
    }
}

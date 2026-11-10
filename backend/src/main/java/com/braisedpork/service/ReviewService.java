package com.braisedpork.service;

import com.braisedpork.dto.ReviewDTO;
import com.braisedpork.dto.ReviewCreateRequest;
import java.util.List;

public interface ReviewService {
    ReviewDTO createReview(Long userId, ReviewCreateRequest request);
    List<ReviewDTO> getProductReviews(Long productId);
    boolean hasUserReviewed(Long userId, Long orderId, Long productId);
}

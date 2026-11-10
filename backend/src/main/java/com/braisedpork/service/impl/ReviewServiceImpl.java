package com.braisedpork.service.impl;

import com.braisedpork.dto.ReviewCreateRequest;
import com.braisedpork.dto.ReviewDTO;
import com.braisedpork.entity.Order;
import com.braisedpork.entity.OrderItem;
import com.braisedpork.entity.Review;
import com.braisedpork.entity.User;
import com.braisedpork.exception.ConflictException;
import com.braisedpork.exception.ForbiddenOperationException;
import com.braisedpork.exception.ResourceNotFoundException;
import com.braisedpork.repository.OrderItemRepository;
import com.braisedpork.repository.OrderRepository;
import com.braisedpork.repository.ReviewRepository;
import com.braisedpork.repository.UserRepository;
import com.braisedpork.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewDTO createReview(Long userId, ReviewCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("无权评价该订单");
        }

        if (order.getStatus() != Order.Status.COMPLETED) {
            throw new ConflictException("只有已完成订单可以评价");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        boolean productBelongsToOrder = items.stream()
                .anyMatch(item -> item.getProductId().equals(request.getProductId()));
        if (!productBelongsToOrder) {
            throw new ConflictException("该商品不属于当前订单");
        }
        if (reviewRepository.existsByUserIdAndOrderIdAndProductId(
                userId, request.getOrderId(), request.getProductId())) {
            throw new ConflictException("该商品已经评价，不能重复提交");
        }

        Review review = Review.builder()
                .userId(userId)
                .orderId(request.getOrderId())
                .productId(request.getProductId())
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        review = reviewRepository.save(review);

        User user = userRepository.findById(userId).orElse(null);
        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .username(user != null ? user.getUsername() : "Unknown")
                .productId(review.getProductId())
                .orderId(review.getOrderId())
                .rating(review.getRating())
                .content(review.getContent())
                .createTime(review.getCreateTime())
                .build();
    }

    @Override
    public List<ReviewDTO> getProductReviews(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreateTimeDesc(productId);
        return reviews.stream().map(r -> {
            User user = userRepository.findById(r.getUserId()).orElse(null);
            return ReviewDTO.builder()
                    .id(r.getId())
                    .userId(r.getUserId())
                    .username(user != null ? user.getUsername() : "Unknown")
                    .productId(r.getProductId())
                    .orderId(r.getOrderId())
                    .rating(r.getRating())
                    .content(r.getContent())
                    .createTime(r.getCreateTime())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public boolean hasUserReviewed(Long userId, Long orderId, Long productId) {
        return reviewRepository.existsByUserIdAndOrderIdAndProductId(userId, orderId, productId);
    }
}

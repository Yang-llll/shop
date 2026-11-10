package com.braisedpork.repository;

import com.braisedpork.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdOrderByCreateTimeDesc(Long productId);
    Optional<Review> findByUserIdAndOrderId(Long userId, Long orderId);
    boolean existsByUserIdAndOrderId(Long userId, Long orderId);
    boolean existsByUserIdAndOrderIdAndProductId(Long userId, Long orderId, Long productId);
}

package com.braisedpork.repository;

import com.braisedpork.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    Optional<Order> findByOrderNo(String orderNo);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    Page<Order> findByStatus(Order.Status status, Pageable pageable);

    long countByStatus(Order.Status status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, Order.Status status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN ('PAID', 'SHIPPED', 'COMPLETED')")
    BigDecimal sumTotalSales();
}
